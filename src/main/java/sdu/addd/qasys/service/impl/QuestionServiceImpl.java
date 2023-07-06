package sdu.addd.qasys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sdu.addd.qasys.common.PageQuery;
import sdu.addd.qasys.common.ResultCode;
import sdu.addd.qasys.config.SystemConfig;
import sdu.addd.qasys.dto.AuthUser;
import sdu.addd.qasys.dto.QuestionDto;
import sdu.addd.qasys.dto.UserDto;
import sdu.addd.qasys.entity.Category;
import sdu.addd.qasys.entity.Question;
import sdu.addd.qasys.entity.User;
import sdu.addd.qasys.exception.ServiceException;
import sdu.addd.qasys.mapper.QuestionMapper;
import sdu.addd.qasys.service.*;
import sdu.addd.qasys.util.HtmlUtil;
import sdu.addd.qasys.util.PageUtil;
import sdu.addd.qasys.util.SecurityUtil;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, Question> implements QuestionService {
    @Autowired
    private QuestionMapper questionMapper;
    @Autowired
    private UserService userService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private SearchService searchService;
    @Autowired
    private LikeService likeService;
    @Autowired
    private SystemConfig systemConfig;
    @Autowired
    private StatisticsService statisticsService;

    //Question中允许排序的列
    private final Set<String> columnsCanSorted = new HashSet<>(Arrays.asList("create_time", "score"));

    @Override
    @Transactional
    public boolean addQuestion(Question question) {
        //设置作者id
        AuthUser currentUser = SecurityUtil.getCurrentUser();
        question.setAuthorId(currentUser.getId());

        //初始化贴子热度分数
        question.setScore(statisticsService.computeQuestionScore(0, 0, 0, LocalDateTime.now()));

        if(questionMapper.insert(question) == 0){
            return false;
        }

        //解析出引用的图片
        List<String> urls = HtmlUtil.getImageUrls(question.getContent());
        if(urls.size() > systemConfig.getMaxPostImageNum()){
            throw new ServiceException(ResultCode.QUESTION_ERROR, "图片数量超出限制");
        }

        //以同步至ElasticSearch
        try {
            searchService.saveQuestion(question);
        } catch (Throwable e) {
            log.error("储存至ElasticSearch失败");
        }
        return true;
    }

    @Override
    public QuestionDto getQuestion(Long questionId) {
        //浏览量++
        statisticsService.increaseQuestionViewCount(questionId);
        //标记需要更新贴子热度分值
        statisticsService.markQuestionToFreshScore(questionId);

        Question question = questionMapper.selectById(questionId);
        return convertToQuestionDto(question);
    }

    @Override
    public IPage<QuestionDto> listQuestion(PageQuery pageQuery, Long categoryId, Long authorId) {
        LambdaQueryWrapper<Question> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(categoryId != null, Question::getCategoryId, categoryId);
        queryWrapper.eq(authorId != null, Question::getAuthorId, authorId);

        IPage<Question> questionPage = pageQuery.getIPage(columnsCanSorted, "score", false);
        questionPage = questionMapper.selectPage(questionPage, queryWrapper);
        List<QuestionDto> questionDtos = questionPage.getRecords().stream().map(this::convertToQuestionDto).collect(Collectors.toList());
        return PageUtil.replaceRecords(questionPage, questionDtos);
    }

    @Override
    public IPage<QuestionDto> searchQuestion(PageQuery pageQuery, String text, Long categoryId, Long authorId) {
        IPage<Question> questionPage = searchService.searchQuestions(pageQuery, text, authorId, categoryId);
        List<Question> questions = questionPage.getRecords();
        if(questions.isEmpty()) return PageUtil.replaceRecords(questionPage, Collections.emptyList());

        //设置likeCount字段
        List<Long> questionIds = questions.stream().map(Question::getId).collect(Collectors.toList());
        List<Long> likeCountBatch = questionMapper.selectLikeCountBatch(questionIds);
        for(int i = 0; i < questionIds.size(); i++){
            questions.get(i).setLikeCount(likeCountBatch.get(i));
        }

        //to QuestionDto
        List<QuestionDto> questionDtos = questions.stream().map(this::convertToQuestionDto).collect(Collectors.toList());
        return PageUtil.replaceRecords(questionPage, questionDtos);
    }

    @Override
    public boolean removeQuestion(Long categoryId) {
        if(questionMapper.deleteById(categoryId) > 0){
            //发布删除事件，以同步至ElasticSearch
            try {
                searchService.removeQuestion(categoryId);
            } catch (Throwable e) {
                log.error("ES中删除失败");
            }
            return true;
        }

        return false;
    }

    @Override
    public boolean isAuthor(Long categoryId, Long userId) {
        Question questionQuery = new Question();
        questionQuery.setId(categoryId);
        questionQuery.setAuthorId(userId);
        return questionMapper.exists(new QueryWrapper<>(questionQuery));
    }

    private QuestionDto convertToQuestionDto(Question question){
        if(question == null) return null;
        QuestionDto questionDto = new QuestionDto();
        BeanUtils.copyProperties(question, questionDto);

        //作者用户信息
        User user = userService.getUser(question.getAuthorId());
        UserDto userDto = new UserDto();
        BeanUtils.copyProperties(user, userDto);
        questionDto.setAuthor(userDto);

        //板块名称
        Category category = categoryService.getById(question.getCategoryId());
        if(category != null){
            questionDto.setCategoryName(category.getName());
        }

        //当前用户是否已点赞
        Long currentUserId = SecurityUtil.isAuthenticated() ? SecurityUtil.getCurrentUser().getId() : null;
        questionDto.setLiked(likeService.hasLikedQuestion(questionDto.getId(), currentUserId));
        return questionDto;
    }
}
