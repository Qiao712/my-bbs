package github.qiao712.bbs.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import github.qiao712.bbs.config.SystemConfig;
import github.qiao712.bbs.domain.base.PageQuery;
import github.qiao712.bbs.domain.dto.AuthUser;
import github.qiao712.bbs.domain.dto.QuestionDto;
import github.qiao712.bbs.domain.dto.UserDto;
import github.qiao712.bbs.domain.entity.*;
import github.qiao712.bbs.exception.ServiceException;
import github.qiao712.bbs.mapper.AnswerMapper;
import github.qiao712.bbs.mapper.AttachmentMapper;
import github.qiao712.bbs.mapper.CommentMapper;
import github.qiao712.bbs.mapper.QuestionMapper;
import github.qiao712.bbs.mq.QuestionMessageSender;
import github.qiao712.bbs.service.*;
import github.qiao712.bbs.util.HtmlUtil;
import github.qiao712.bbs.util.PageUtil;
import github.qiao712.bbs.util.SecurityUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service("questionService")
@Transactional
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, Question> implements QuestionService {
    @Autowired
    private QuestionMapper questionMapper;
    @Autowired
    private UserService userService;
    @Autowired
    private ForumService forumService;
    @Autowired
    private SearchService searchService;
    @Autowired
    private FileService fileService;
    @Autowired
    private LikeService likeService;
    @Autowired
    private AttachmentMapper attachmentMapper;
    @Autowired
    private CommentMapper commentMapper;
    @Autowired
    private AnswerMapper answerMapper;
    @Autowired
    private SystemConfig systemConfig;
    @Autowired
    private StatisticsService statisticsService;
    @Autowired
    private QuestionMessageSender questionMessageSender;

    //Question中允许排序的列
    private final Set<String> columnsCanSorted = new HashSet<>(Arrays.asList("create_time", "score"));

    //问题中图片的source(标识)
    public final static String QUESTION_IMAGE_SOURCE = "question-image";

    @Override
    @Transactional
    public boolean addQuestion(Question question) {
        //设置作者id
        AuthUser currentUser = SecurityUtil.getCurrentUser();
        question.setAuthorId(currentUser.getId());

        //初始化问题热度分数
        question.setScore(statisticsService.computeQuestionScore(0, 0, 0, LocalDateTime.now()));

        if(questionMapper.insert(question) == 0){
            return false;
        }

        //解析出引用的图片
        List<String> urls = HtmlUtil.getImageUrls(question.getContent());
        if(urls.size() > systemConfig.getMaxQuestionImageNum()){
            throw new ServiceException("图片数量超出限制");
        }

        //如果文件的上传者是该该用户(问题作者)，且上传来源为问题图片，则记录该问题对图片的引用(记录为该问题的一个附件)
        List<Long> imageFileIds = new ArrayList<>(urls.size());
        for (String url : urls) {
            FileIdentity imageFileIdentity = fileService.getFileIdentityByUrl(url);
            if(imageFileIdentity == null) continue;  //为外部链接

            if(Objects.equals(imageFileIdentity.getUploaderId(), currentUser.getId()) && QUESTION_IMAGE_SOURCE.equals(imageFileIdentity.getSource())){
                imageFileIds.add(imageFileIdentity.getId());
            }
        }
        if(!imageFileIds.isEmpty()){
            attachmentMapper.insertAttachments(question.getId(), null, imageFileIds);

            //将引用的图片文件标记为非临时文件，不再进行清理
            fileService.setTempFlags(imageFileIds, false);
        }

        //发布添加事件，以同步至ElasticSearch
        questionMessageSender.sendQuestionAddMessage(question);
        return true;
    }

    @Override
    @Transactional
    public String uploadImage(MultipartFile image) {
        //上传为临时文件
        FileIdentity fileIdentity = fileService.uploadImage(QUESTION_IMAGE_SOURCE, image, systemConfig.getMaxInsertedImageSize(), true);

        if(fileIdentity != null) return fileService.getFileUrl(fileIdentity.getId());
        else return null;
    }

    @Override
    public QuestionDto getQuestion(Long questionId) {
        //浏览量++
        statisticsService.increaseQuestionViewCount(questionId);
        //标记需要更新问题热度分值
        statisticsService.markQuestionToFreshScore(questionId);

        Question question = questionMapper.selectById(questionId);
        Long currentUserId = SecurityUtil.isAuthenticated() ? SecurityUtil.getCurrentUser().getId() : null;
        return questionDtoMap(question, currentUserId);
    }

    @Override
    public IPage<QuestionDto> listQuestion(PageQuery pageQuery, Long forumId, String authorUsername) {
        LambdaQueryWrapper<Question> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(forumId != null, Question::getForumId, forumId);
        if(authorUsername != null){
            Long authorId = userService.getUserIdByUsername(authorUsername);
            queryWrapper.eq(Question::getAuthorId, authorId);
        }

        IPage<Question> questionPage = pageQuery.getIPage(columnsCanSorted, "score", false);

        questionPage = questionMapper.selectPage(questionPage, queryWrapper);

        //to QuestionDto
        List<Question> questions = questionPage.getRecords();
        List<QuestionDto> questionDtos = new ArrayList<>(questions.size());
        Long currentUserId = SecurityUtil.isAuthenticated() ? SecurityUtil.getCurrentUser().getId() : null;
        for (Question question : questions) {
            questionDtos.add(questionDtoMap(question, currentUserId));
        }

        return PageUtil.replaceRecords(questionPage, questionDtos);
    }

    @Override
    public IPage<QuestionDto> searchQuestions(PageQuery pageQuery, String text, Long forumId, Long authorId) {
        IPage<Question> questionPage = searchService.searchQuestions(pageQuery, text, authorId, forumId);
        List<Question> questions = questionPage.getRecords();
        if(questions.isEmpty()) return PageUtil.replaceRecords(questionPage, Collections.emptyList());

        //设置likeCount字段
        List<Long> questionIds = questions.stream().map(Question::getId).collect(Collectors.toList());
        List<Long> likeCountBatch = questionMapper.selectLikeCountBatch(questionIds);
        for(int i = 0; i < questionIds.size(); i++){
            questions.get(i).setLikeCount(likeCountBatch.get(i));
        }

        //to QuestionDto
        List<QuestionDto> questionDtos = new ArrayList<>(questions.size());
        Long currentUserId = SecurityUtil.isAuthenticated() ? SecurityUtil.getCurrentUser().getId() : null;
        for (Question question : questions) {
            questionDtos.add(questionDtoMap(question, currentUserId));
        }

        return PageUtil.replaceRecords(questionPage, questionDtos);
    }

    @Override
    public boolean removeQuestion(Long questionId) {
        //删除所有附件
        //标记其引用的图片(附件)可以清理
        List<Long> attachmentFileIds = attachmentMapper.selectAttachmentFileIdsOfQuestion(questionId);
        if(!attachmentFileIds.isEmpty()){
            fileService.setTempFlags(attachmentFileIds, true);
        }
        //删除attachment记录
        Attachment attachmentQuery = new Attachment();
        attachmentQuery.setQuestionId(questionId);
        attachmentMapper.delete(new QueryWrapper<>(attachmentQuery));

        //所有回答相关评论
        commentMapper.deleteByQuestionId(questionId);
        //删除所有回答
        LambdaQueryWrapper<Answer> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Answer::getQuestionId, questionId);
        answerMapper.delete(queryWrapper);

        if(questionMapper.deleteById(questionId) > 0){
            //发布删除事件，以同步至ElasticSearch
            questionMessageSender.sendQuestionDeleteMessage(questionId);
            return true;
        }

        return false;
    }

    @Override
    public boolean isAuthor(Long questionId, Long userId) {
        Question questionQuery = new Question();
        questionQuery.setId(questionId);
        questionQuery.setAuthorId(userId);
        return questionMapper.exists(new QueryWrapper<>(questionQuery));
    }

    private QuestionDto questionDtoMap(Question question, Long currentUserId){
        if(question == null) return null;
        QuestionDto questionDto = new QuestionDto();
        BeanUtils.copyProperties(question, questionDto);

        //作者用户信息
        User user = userService.getUser(question.getAuthorId());
        UserDto userDto = new UserDto();
        BeanUtils.copyProperties(user, userDto);
        questionDto.setAuthor(userDto);

        //板块名称
        Forum forum = forumService.getById(question.getForumId());
        questionDto.setForumName(forum.getName());

        //当前用户是否已点赞
        questionDto.setLiked(likeService.hasLikedQuestion(question.getId(), currentUserId));

        //查询Redis缓存的最新的点赞数量
        Long likeCount = likeService.getQuestionLikeCountFromCache(question.getId());
        if(likeCount != null){
            questionDto.setLikeCount(likeCount);
        }
        return questionDto;
    }
}
