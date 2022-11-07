package github.qiao712.bbs.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import github.qiao712.bbs.config.SystemConfig;
import github.qiao712.bbs.domain.base.PageQuery;
import github.qiao712.bbs.domain.dto.AuthUser;
import github.qiao712.bbs.domain.dto.PostDto;
import github.qiao712.bbs.domain.dto.UserDto;
import github.qiao712.bbs.domain.entity.*;
import github.qiao712.bbs.exception.ServiceException;
import github.qiao712.bbs.mapper.AttachmentMapper;
import github.qiao712.bbs.mapper.CommentMapper;
import github.qiao712.bbs.mapper.PostMapper;
import github.qiao712.bbs.mq.PostMessageSender;
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

@Service("postService")
@Transactional
public class PostServiceImpl extends ServiceImpl<PostMapper, Post> implements PostService {
    @Autowired
    private PostMapper postMapper;
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
    private SystemConfig systemConfig;
    @Autowired
    private StatisticsService statisticsService;
    @Autowired
    private PostMessageSender postMessageSender;

    //Post中允许排序的列
    private final Set<String> columnsCanSorted = new HashSet<>(Arrays.asList("create_time", "score"));

    //贴子中图片的source(标识)
    public final static String POST_IMAGE_SOURCE = "post-image";

    @Override
    @Transactional
    public boolean addPost(Post post) {
        //设置作者id
        AuthUser currentUser = SecurityUtil.getCurrentUser();
        post.setAuthorId(currentUser.getId());

        //初始化贴子热度分数
        post.setScore(statisticsService.computePostScore(0, 0, 0, LocalDateTime.now()));

        if(postMapper.insert(post) == 0){
            return false;
        }

        //解析出引用的图片
        List<String> urls = HtmlUtil.getImageUrls(post.getContent());
        if(urls.size() > systemConfig.getMaxPostImageNum()){
            throw new ServiceException("图片数量超出限制");
        }

        //如果文件的上传者是该该用户(贴子作者)，且上传来源为贴子图片，则记录该贴子对图片的引用(记录为该贴子的一个附件)
        List<Long> imageFileIds = new ArrayList<>(urls.size());
        for (String url : urls) {
            FileIdentity imageFileIdentity = fileService.getFileIdentityByUrl(url);
            if(imageFileIdentity == null) continue;  //为外部链接

            if(Objects.equals(imageFileIdentity.getUploaderId(), currentUser.getId()) && POST_IMAGE_SOURCE.equals(imageFileIdentity.getSource())){
                imageFileIds.add(imageFileIdentity.getId());
            }
        }
        if(!imageFileIds.isEmpty()){
            attachmentMapper.insertAttachments(post.getId(), null, imageFileIds);

            //将引用的图片文件标记为非临时文件，不再进行清理
            fileService.setTempFlags(imageFileIds, false);
        }

        //发布添加事件，以同步至ElasticSearch
        postMessageSender.sendPostAddMessage(post);
        return true;
    }

    @Override
    @Transactional
    public String uploadImage(MultipartFile image) {
        //上传为临时文件
        FileIdentity fileIdentity = fileService.uploadImage(POST_IMAGE_SOURCE, image, systemConfig.getMaxPostImageSize(), true);

        if(fileIdentity != null) return fileService.getFileUrl(fileIdentity.getId());
        else return null;
    }

    @Override
    public PostDto getPost(Long postId) {
        //浏览量++
        statisticsService.increasePostViewCount(postId);
        //标记需要更新贴子热度分值
        statisticsService.markPostToFreshScore(postId);

        Post post = postMapper.selectById(postId);
        Long currentUserId = SecurityUtil.isAuthenticated() ? SecurityUtil.getCurrentUser().getId() : null;
        return postDtoMap(post, currentUserId);
    }

    @Override
    public IPage<PostDto> listPosts(PageQuery pageQuery, Long forumId, String authorUsername) {
        LambdaQueryWrapper<Post> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(forumId != null, Post::getForumId, forumId);
        if(authorUsername != null){
            Long authorId = userService.getUserIdByUsername(authorUsername);
            queryWrapper.eq(Post::getAuthorId, authorId);
        }

        IPage<Post> postPage = pageQuery.getIPage(columnsCanSorted, "score", false);

        postPage = postMapper.selectPage(postPage, queryWrapper);

        //to PostDto
        List<Post> posts = postPage.getRecords();
        List<PostDto> postDtos = new ArrayList<>(posts.size());
        Long currentUserId = SecurityUtil.isAuthenticated() ? SecurityUtil.getCurrentUser().getId() : null;
        for (Post post : posts) {
            postDtos.add(postDtoMap(post, currentUserId));
        }

        return PageUtil.replaceRecords(postPage, postDtos);
    }

    @Override
    public IPage<PostDto> searchPosts(PageQuery pageQuery, String text, Long forumId, Long authorId) {
        IPage<Post> postPage = searchService.searchPosts(pageQuery, text, authorId, forumId);
        List<Post> posts = postPage.getRecords();
        if(posts.isEmpty()) return PageUtil.replaceRecords(postPage, Collections.emptyList());

        //设置likeCount字段
        List<Long> postIds = posts.stream().map(Post::getId).collect(Collectors.toList());
        List<Long> likeCountBatch = postMapper.selectLikeCountBatch(postIds);
        for(int i = 0; i < postIds.size(); i++){
            posts.get(i).setLikeCount(likeCountBatch.get(i));
        }

        //to PostDto
        List<PostDto> postDtos = new ArrayList<>(posts.size());
        Long currentUserId = SecurityUtil.isAuthenticated() ? SecurityUtil.getCurrentUser().getId() : null;
        for (Post post : posts) {
            postDtos.add(postDtoMap(post, currentUserId));
        }

        return PageUtil.replaceRecords(postPage, postDtos);
    }

    @Override
    public boolean removePost(Long postId) {
        //删除所有附件
        //标记其引用的图片(附件)可以清理
        List<Long> attachmentFileIds = attachmentMapper.selectAttachmentFileIdsOfPost(postId);
        if(!attachmentFileIds.isEmpty()){
            fileService.setTempFlags(attachmentFileIds, true);
        }
        //删除attachment记录
        Attachment attachmentQuery = new Attachment();
        attachmentQuery.setPostId(postId);
        attachmentMapper.delete(new QueryWrapper<>(attachmentQuery));

        //删除所有评论
        Comment commentQuery = new Comment();
        commentQuery.setPostId(postId);
        commentMapper.delete(new QueryWrapper<>(commentQuery));

        if(postMapper.deleteById(postId) > 0){
            //发布删除事件，以同步至ElasticSearch
            postMessageSender.sendPostDeleteMessage(postId);
            return true;
        }

        return false;
    }

    @Override
    public boolean isAuthor(Long postId, Long userId) {
        Post postQuery = new Post();
        postQuery.setId(postId);
        postQuery.setAuthorId(userId);
        return postMapper.exists(new QueryWrapper<>(postQuery));
    }

    private PostDto postDtoMap(Post post, Long currentUserId){
        if(post == null) return null;
        PostDto postDto = new PostDto();
        BeanUtils.copyProperties(post, postDto);

        //作者用户信息
        User user = userService.getUser(post.getAuthorId());
        UserDto userDto = new UserDto();
        BeanUtils.copyProperties(user, userDto);
        postDto.setAuthor(userDto);

        //板块名称
        Forum forum = forumService.getById(post.getForumId());
        postDto.setForumName(forum.getName());

        //当前用户是否已点赞
        postDto.setLiked(likeService.hasLikedPost(post.getId(), currentUserId));

        //查询Redis缓存的最新的点赞数量
        Long likeCount = likeService.getPostLikeCount(post.getId());
        if(likeCount != null){
            postDto.setLikeCount(likeCount);
        }
        return postDto;
    }
}
