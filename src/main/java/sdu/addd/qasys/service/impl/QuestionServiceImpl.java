package sdu.addd.qasys.service.impl;

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
import sdu.addd.qasys.entity.Question;
import sdu.addd.qasys.entity.Tag;
import sdu.addd.qasys.entity.TagRelation;
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
    private UserService userService;
    @Autowired
    private TagService tagService;
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
        if(question.getTags().size() < 1 || question.getTags().size() > 10){
            throw new ServiceException(ResultCode.QUESTION_ERROR, "标签个数必须在1到10之间");
        }

        //设置作者id
        AuthUser currentUser = SecurityUtil.getCurrentUser();
        question.setAuthorId(currentUser.getId());

        //初始化贴子热度分数
        question.setScore(statisticsService.computeQuestionScore(0, 0, 0, LocalDateTime.now()));

        if(!save(question)){
            throw new ServiceException(ResultCode.QUESTION_ERROR, "问题发布失败");
        }

        //解析出引用的图片
        List<String> urls = HtmlUtil.getImageUrls(question.getContent());
        if(urls.size() > systemConfig.getMaxPostImageNum()){
            throw new ServiceException(ResultCode.QUESTION_ERROR, "图片数量超出限制");
        }

        //绑定标签
        List<TagRelation> tagRelations = question.getTags().stream().map(tagName -> {
            Tag tag = tagService.getOrCreateTagByName(tagName);
            return new TagRelation(tag.getId(), question.getId());
        }).collect(Collectors.toList());
        tagService.saveTagRelations(tagRelations);

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

        Question question = getById(questionId);
        return convertToQuestionDto(question);
    }

    @Override
    public IPage<QuestionDto> listQuestion(PageQuery pageQuery, Long tagId, Long authorId) {
        IPage<Question> questionPage = pageQuery.getIPage(columnsCanSorted, "score", false);
        questionPage = baseMapper.selectQuestions(questionPage, tagId, authorId);
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
        List<Long> likeCountBatch = baseMapper.selectLikeCountBatch(questionIds);
        for(int i = 0; i < questionIds.size(); i++){
            questions.get(i).setLikeCount(likeCountBatch.get(i));
        }

        //to QuestionDto
        List<QuestionDto> questionDtos = questions.stream().map(this::convertToQuestionDto).collect(Collectors.toList());
        return PageUtil.replaceRecords(questionPage, questionDtos);
    }

    @Override
    public boolean removeQuestion(Long questionId) {
        //删除标签
        tagService.removeTagRelationsByQuestionId(questionId);

        if(removeById(questionId)){
            //发布删除事件，以同步至ElasticSearch
            try {
                searchService.removeQuestion(questionId);
            } catch (Throwable e) {
                log.error("ElasticSearch文档删除失败");
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

        return baseMapper.exists(new QueryWrapper<>(questionQuery));
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

        //标签
        questionDto.setTags(tagService.getTagsOfQuestion(question.getId()));

        //当前用户是否已点赞
        Long currentUserId = SecurityUtil.isAuthenticated() ? SecurityUtil.getCurrentUser().getId() : null;
        questionDto.setLiked(likeService.hasLikedQuestion(questionDto.getId(), currentUserId));
        return questionDto;
    }
}
