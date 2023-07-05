package github.qiao712.bbs.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import github.qiao712.bbs.config.SystemConfig;
import github.qiao712.bbs.domain.base.PageQuery;
import github.qiao712.bbs.domain.base.ResultCode;
import github.qiao712.bbs.domain.dto.AuthUser;
import github.qiao712.bbs.domain.dto.PostDto;
import github.qiao712.bbs.domain.dto.UserDto;
import github.qiao712.bbs.domain.entity.Category;
import github.qiao712.bbs.domain.entity.Question;
import github.qiao712.bbs.domain.entity.User;
import github.qiao712.bbs.exception.ServiceException;
import github.qiao712.bbs.mapper.QuestionMapper;
import github.qiao712.bbs.service.*;
import github.qiao712.bbs.util.HtmlUtil;
import github.qiao712.bbs.util.PageUtil;
import github.qiao712.bbs.util.SecurityUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    //Post中允许排序的列
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
            throw new ServiceException(ResultCode.POST_ERROR, "图片数量超出限制");
        }

        //以同步至ElasticSearch
        try {
            searchService.savePost(question);
        } catch (Throwable e) {
            log.error("储存至ElasticSearch失败");
        }
        return true;
    }

    @Override
    public PostDto getQuestion(Long postId) {
        //浏览量++
        statisticsService.increaseQuestionViewCount(postId);
        //标记需要更新贴子热度分值
        statisticsService.markQuestionToFreshScore(postId);

        Question question = questionMapper.selectById(postId);
        return convertToPostDto(question);
    }

    @Override
    public IPage<PostDto> listQuestion(PageQuery pageQuery, Long forumId, Long authorId) {
        LambdaQueryWrapper<Question> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(forumId != null, Question::getCategoryId, forumId);
        queryWrapper.eq(authorId != null, Question::getAuthorId, authorId);

        IPage<Question> postPage = pageQuery.getIPage(columnsCanSorted, "score", false);
        postPage = questionMapper.selectPage(postPage, queryWrapper);
        List<PostDto> postDtos = postPage.getRecords().stream().map(this::convertToPostDto).collect(Collectors.toList());
        return PageUtil.replaceRecords(postPage, postDtos);
    }

    @Override
    public IPage<PostDto> searchQuestion(PageQuery pageQuery, String text, Long forumId, Long authorId) {
        IPage<Question> postPage = searchService.searchPosts(pageQuery, text, authorId, forumId);
        List<Question> questions = postPage.getRecords();
        if(questions.isEmpty()) return PageUtil.replaceRecords(postPage, Collections.emptyList());

        //设置likeCount字段
        List<Long> postIds = questions.stream().map(Question::getId).collect(Collectors.toList());
        List<Long> likeCountBatch = questionMapper.selectLikeCountBatch(postIds);
        for(int i = 0; i < postIds.size(); i++){
            questions.get(i).setLikeCount(likeCountBatch.get(i));
        }

        //to PostDto
        List<PostDto> postDtos = questions.stream().map(this::convertToPostDto).collect(Collectors.toList());
        return PageUtil.replaceRecords(postPage, postDtos);
    }

    @Override
    public boolean removeQuestion(Long postId) {
        if(questionMapper.deleteById(postId) > 0){
            //发布删除事件，以同步至ElasticSearch
            try {
                searchService.removePost(postId);
            } catch (Throwable e) {
                log.error("ES中删除失败");
            }
            return true;
        }

        return false;
    }

    @Override
    public boolean isAuthor(Long postId, Long userId) {
        Question questionQuery = new Question();
        questionQuery.setId(postId);
        questionQuery.setAuthorId(userId);
        return questionMapper.exists(new QueryWrapper<>(questionQuery));
    }

    private PostDto convertToPostDto(Question question){
        if(question == null) return null;
        PostDto postDto = new PostDto();
        BeanUtils.copyProperties(question, postDto);

        //作者用户信息
        User user = userService.getUser(question.getAuthorId());
        UserDto userDto = new UserDto();
        BeanUtils.copyProperties(user, userDto);
        postDto.setAuthor(userDto);

        //板块名称
        Category category = categoryService.getById(question.getCategoryId());
        postDto.setForumName(category.getName());

        //当前用户是否已点赞
        Long currentUserId = SecurityUtil.isAuthenticated() ? SecurityUtil.getCurrentUser().getId() : null;
        postDto.setLiked(likeService.hasLikedQuestion(postDto.getId(), currentUserId));
        return postDto;
    }
}
