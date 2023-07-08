package sdu.addd.qasys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sdu.addd.qasys.common.PageQuery;
import sdu.addd.qasys.common.ResultCode;
import sdu.addd.qasys.dto.AuthUser;
import sdu.addd.qasys.dto.UserDto;
import sdu.addd.qasys.dto.message.AnswerMessageContent;
import sdu.addd.qasys.entity.Answer;
import sdu.addd.qasys.entity.Question;
import sdu.addd.qasys.entity.User;
import sdu.addd.qasys.exception.ServiceException;
import sdu.addd.qasys.mapper.AnswerMapper;
import sdu.addd.qasys.mapper.QuestionMapper;
import sdu.addd.qasys.service.AnswerService;
import sdu.addd.qasys.service.MessageService;
import sdu.addd.qasys.service.StatisticsService;
import sdu.addd.qasys.service.UserService;
import sdu.addd.qasys.util.HtmlUtil;
import sdu.addd.qasys.util.SecurityUtil;

import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 答案 服务实现类
 * </p>
 *
 * @author qiao712
 * @since 2023-07-05
 */
@Service
public class AnswerServiceImpl extends ServiceImpl<AnswerMapper, Answer> implements AnswerService {
    @Autowired
    private QuestionMapper questionMapper;
    @Autowired
    private UserService userService;
    @Autowired
    private StatisticsService statisticsService;
    @Autowired
    private MessageService messageService;

    @Override
    public boolean addAnswer(Answer answer) {
        AuthUser currentUser = SecurityUtil.getCurrentUser();
        answer.setAuthorId(currentUser.getId());

        QueryWrapper<Question> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", answer.getQuestionId());
        if(!questionMapper.exists(queryWrapper)){
            throw new ServiceException(ResultCode.INVALID_PARAM, "问题不存在");
        }

        //创建评论，获取主键(插入附加时使用)
        boolean flag = baseMapper.insert(answer) > 0;

        //贴子评论数+1
        questionMapper.increaseAnswerCount(answer.getQuestionId(), 1L);

        //发送评论/回复消息
        sendNoticeMessage(answer);

        //标记贴子需要刷新热度值
        statisticsService.markQuestionToFreshScore(answer.getQuestionId());

        return flag;
    }

    @Override
    public IPage<Answer> listAnswers(PageQuery pageQuery, Long questionId) {
        LambdaQueryWrapper<Answer> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Answer::getQuestionId, questionId);
        IPage<Answer> answerIPage = baseMapper.selectPage(pageQuery.getIPage(), queryWrapper);
        List<Answer> answers = answerIPage.getRecords();

        //将所用到的用户信息一次查出
        if(answers != null){
            Set<Long> authorIds = answers.stream().map(Answer::getAuthorId).collect(Collectors.toSet());
            Map<Long, UserDto> userDtoMap = new HashMap<>(authorIds.size());
            for (Long authorId : authorIds) {
                User user = userService.getUser(authorId);
                UserDto userDto = new UserDto();
                BeanUtils.copyProperties(user, userDto);
                userDtoMap.put(authorId, userDto);
            }

            for (Answer answer : answers) {
                answer.setAuthor(userDtoMap.get(answer.getAuthorId()));
            }
        }

        return answerIPage;
    }

    @Override
    public IPage<Answer> listAnswersByAuthor(PageQuery pageQuery, Long id) {
        LambdaQueryWrapper<Answer> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Answer::getAuthorId, id);
        IPage<Answer> answerIPage = baseMapper.selectPage(pageQuery.getIPage(), queryWrapper);
        List<Answer> answers = answerIPage.getRecords();

        //将所用到的用户信息一次查出
        if(answers != null){
            Set<Long> authorIds = answers.stream().map(Answer::getAuthorId).collect(Collectors.toSet());
            Map<Long, UserDto> userDtoMap = new HashMap<>(authorIds.size());
            for (Long authorId : authorIds) {
                User user = userService.getUser(authorId);
                UserDto userDto = new UserDto();
                BeanUtils.copyProperties(user, userDto);
                userDtoMap.put(authorId, userDto);
            }

            for (Answer answer : answers) {
                answer.setAuthor(userDtoMap.get(answer.getAuthorId()));
            }
        }

        return answerIPage;
    }

    @Override
    public boolean removeAnswer(Long answerId) {
        return baseMapper.deleteById(answerId) > 0;
    }

    @Override
    public boolean isAuthor(Long answerId, Long userId) {
        Answer answer = new Answer();
        answer.setId(answerId);
        answer.setAuthorId(userId);
        return baseMapper.exists(new QueryWrapper<>(answer));
    }

    /**
     * 发送评论提醒消息
     */
    private void sendNoticeMessage(Answer answer){
        AnswerMessageContent answerMessageContent = new AnswerMessageContent();

        answerMessageContent.setAnswerId(answer.getId());
        answerMessageContent.setAuthorId(answer.getAuthorId());
        answerMessageContent.setAuthorUsername(userService.getUsername(answer.getAuthorId()));
        answerMessageContent.setQuestionId(answer.getQuestionId());

        //设置问题标题
        LambdaQueryWrapper<Question> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Question::getId, answer.getQuestionId());
        queryWrapper.select(Question::getTitle, Question::getAuthorId);
        Question question = questionMapper.selectOne(queryWrapper);
        answerMessageContent.setQuestionTitle(question.getTitle());
        answerMessageContent.setAnswer(HtmlUtil.getText(answer.getContent()));

        if(!Objects.equals(question.getAuthorId(), answer.getAuthorId())){
            //使用回答的id作为消息的key，以便快速检索删除
            messageService.sendMessage(answer.getAuthorId(), question.getAuthorId(), answer.getId().toString(), answerMessageContent);
        }
    }
}
