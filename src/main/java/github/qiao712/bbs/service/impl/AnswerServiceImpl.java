package github.qiao712.bbs.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import github.qiao712.bbs.config.SystemConfig;
import github.qiao712.bbs.domain.base.PageQuery;
import github.qiao712.bbs.domain.dto.*;
import github.qiao712.bbs.domain.entity.*;
import github.qiao712.bbs.exception.ServiceException;
import github.qiao712.bbs.mapper.AnswerMapper;
import github.qiao712.bbs.mapper.AttachmentMapper;
import github.qiao712.bbs.mapper.CommentMapper;
import github.qiao712.bbs.mapper.QuestionMapper;
import github.qiao712.bbs.mq.CommentMessageSender;
import github.qiao712.bbs.service.*;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import github.qiao712.bbs.util.HtmlUtil;
import github.qiao712.bbs.util.PageUtil;
import github.qiao712.bbs.util.SecurityUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class AnswerServiceImpl extends ServiceImpl<AnswerMapper, Answer> implements AnswerService {
    @Autowired
    private AnswerMapper answerMapper;
    @Autowired
    private CommentMapper commentMapper;
    @Autowired
    private QuestionMapper questionMapper;
    @Autowired
    private FileService fileService;
    @Autowired
    private UserService userService;
    @Autowired
    private LikeService likeService;
    @Autowired
    private AttachmentMapper attachmentMapper;
    @Autowired
    private StatisticsService statisticsService;
    @Autowired
    private SystemConfig systemConfig;
    @Autowired
    private CommentMessageSender commentMessageSender;

    @Override
    public boolean addAnswer(Answer answer) {
        AuthUser currentUser = SecurityUtil.getCurrentUser();
        answer.setAuthorId(currentUser.getId());

        LambdaQueryWrapper<Question> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Question::getId, answer.getQuestionId());
        if(!questionMapper.exists(queryWrapper)){
            throw new ServiceException("目标问题不存在");
        }

        //创建回答，获取主键(插入附件时使用)
        if(answerMapper.insert(answer) == 0){
            return false;
        }

        //解析出引用的图片
        List<String> urls = HtmlUtil.getImageUrls(answer.getContent());
        if(urls.size() > systemConfig.getMaxAnswerImageNum()){
            throw new ServiceException("图片数量超出限制");
        }

        //如果文件的上传者是该用户(回答作者)，则记录该回答对图片的引用(记录为该回答一个附件)
        List<Long> imageFileIds = new ArrayList<>(urls.size());
        for (String url : urls) {
            FileIdentity imageFileIdentity = fileService.getFileIdentityByUrl(url);
            if(imageFileIdentity == null) continue;  //为外部连接

            if(Objects.equals(imageFileIdentity.getUploaderId(), currentUser.getId())
                    && QuestionServiceImpl.QUESTION_IMAGE_SOURCE.equals(imageFileIdentity.getSource())){
                imageFileIds.add(imageFileIdentity.getId());
            }
        }
        if(!imageFileIds.isEmpty()){
            attachmentMapper.insertAttachments(answer.getQuestionId(), answer.getId(), imageFileIds);
            //将引用的图片文件标记为非临时文件，不再进行清理
            fileService.setTempFlags(imageFileIds, false);
        }

        //问题回答数+1
        questionMapper.increaseAnswerCount(answer.getQuestionId(), 1L);

        //发送回答/回复消息
        //TODO:
//        commentMessageSender.sendCommentAddMessage(answer);

        //标记问题需要刷新热度值
        statisticsService.markQuestionToFreshScore(answer.getQuestionId());

        return true;
    }

    @Override
    public boolean removeAnswer(Long answerId) {
        Answer answer = answerMapper.selectById(answerId);
        if(answer == null) return false;
        long deletedCommentCount = 0;

        //删除评论
        LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Comment::getAnswerId, answerId);
        deletedCommentCount += commentMapper.delete(queryWrapper);

        //标记其引用的图片(附件)可以清理
        List<Long> attachmentFileIds = attachmentMapper.selectAttachmentFileIdsOfComment(answer.getQuestionId(), answer.getId());
        if(!attachmentFileIds.isEmpty())
            fileService.setTempFlags(attachmentFileIds, true);

        //删除attachment记录
        Attachment attachmentQuery = new Attachment();
        attachmentQuery.setQuestionId(answer.getQuestionId());
        attachmentQuery.setCommentId(answer.getId());
        attachmentMapper.delete(new QueryWrapper<>(attachmentQuery));

        //更新评论数量
        questionMapper.increaseAnswerCount(answer.getQuestionId(), -deletedCommentCount - 1);

        //标记问题需要刷新热度值
        statisticsService.markQuestionToFreshScore(answer.getQuestionId());

        return answerMapper.deleteById(answerId) > 0;
    }

    @Override
    public IPage<AnswerDto> listAnswers(PageQuery pageQuery, Long questionId) {
        LambdaQueryWrapper<Answer> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Answer::getQuestionId, questionId);
        queryWrapper.orderByAsc(Answer::getCreateTime);
        IPage<Answer> answerPage = answerMapper.selectPage(pageQuery.getIPage(), queryWrapper);
        List<Answer> answers = answerPage.getRecords();

        //将所用到的用户信息一次查出
        Set<Long> authorIds = answers.stream().map(Answer::getAuthorId).collect(Collectors.toSet());
        Map<Long, UserDto> userDtoMap = new HashMap<>(authorIds.size());
        for (Long authorId : authorIds) {
            User user = userService.getUser(authorId);
            UserDto userDto = new UserDto();
            BeanUtils.copyProperties(user, userDto);
            userDtoMap.put(authorId, userDto);
        }

        //当前用户ID，用于判断是否已点赞
        Long currentUserId = SecurityUtil.isAuthenticated() ? SecurityUtil.getCurrentUser().getId() : null;

        //组装AnswerDto
        List<AnswerDto> answerDtos = new ArrayList<>();
        for (Answer answer : answers) {
            AnswerDto answerDto = new AnswerDto();
            BeanUtils.copyProperties(answer, answerDto);

            //author
            answerDto.setAuthor(userDtoMap.get(answer.getAuthorId()));
            //当前用户是否点赞
            answerDto.setLiked(likeService.hasLikedComment(answer.getId(), currentUserId));

            answerDtos.add(answerDto);
        }

        return PageUtil.replaceRecords(answerPage, answerDtos);
    }

    @Override
    public IPage<AnswerDetailDto> listAnswersByAuthor(PageQuery pageQuery, String authorUsername) {
        Long authorId = null;
        if(authorUsername != null){
            authorId = userService.getUserIdByUsername(authorUsername);
        }

        LambdaQueryWrapper<Answer> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Answer::getAuthorId, authorId);
        queryWrapper.orderByAsc(Answer::getCreateTime);
        IPage<Answer> answerPage = answerMapper.selectPage(pageQuery.getIPage(), queryWrapper);
        List<Answer> answers = answerPage.getRecords();

        //聚合简略的问题信息
        List<Long> questionIds = answers.stream().map(Answer::getQuestionId).collect(Collectors.toList());
        LambdaQueryWrapper<Question> questionQueryWrapper = new LambdaQueryWrapper<>();
        questionQueryWrapper.in(Question::getId, questionIds);
        questionQueryWrapper.select(Question::getId, Question::getTitle);
        List<Question> questions = questionMapper.selectBatchIds(questionIds);
        Map<Long, Question> questionMap = new HashMap<>();
        for (Question question : questions) {
            questionMap.put(question.getId(), question);
        }

        //当前用户ID，用于判断是否已点赞
        Long currentUserId = SecurityUtil.isAuthenticated() ? SecurityUtil.getCurrentUser().getId() : null;

        List<AnswerDetailDto> answerDetailDtos = new ArrayList<>(answers.size());
        for (Answer answer : answers) {
            AnswerDetailDto answerDetailDto = new AnswerDetailDto();
            BeanUtils.copyProperties(answer, answerDetailDto);

            answerDetailDto.setQuestionTitle(questionMap.get(answer.getQuestionId()).getTitle());
            answerDetailDto.setLiked(likeService.hasLikedComment(answer.getId(), currentUserId));
        }


        return PageUtil.replaceRecords(answerPage, answerDetailDtos);
    }

    @Override
    public boolean isAuthor(Long answerId, Long userId) {
        Answer answerQuery = new Answer();
        answerQuery.setId(answerId);
        answerQuery.setAuthorId(userId);
        return answerMapper.exists(new QueryWrapper<>(answerQuery));
    }
}
