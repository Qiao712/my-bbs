package github.qiao712.bbs.service.impl;

import com.alibaba.fastjson.JSON;
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
import github.qiao712.bbs.mq.post.PostMessageSender;
import github.qiao712.bbs.service.*;
import github.qiao712.bbs.util.HtmlUtil;
import github.qiao712.bbs.util.PageUtil;
import github.qiao712.bbs.util.SecurityUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
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

    @Autowired
    private StringRedisTemplate redisTemplate;

    private final String POST_CACHE_KEY_PREFIX = "post-";
    private final int CACHE_EXPIRE_SECONDS = 60;            //?????????????????? 60s
    private final int CACHED_POST_LISTS_LENGTH = 200;       //???????????????????????????CACHED_POST_LISTS_LENGTH?????????????????????

    //Post?????????????????????
    private final Set<String> columnsCanSorted = new HashSet<>(Arrays.asList("create_time", "score"));

    @Override
    @Transactional
    public boolean addPost(Post post) {
        //????????????id
        AuthUser currentUser = SecurityUtil.getCurrentUser();
        post.setAuthorId(currentUser.getId());

        //???????????????????????????
        post.setScore(statisticsService.computePostScore(0, 0, 0, LocalDateTime.now()));

        if(postMapper.insert(post) == 0){
            return false;
        }

        //????????????????????????
        List<String> urls = HtmlUtil.getImageUrls(post.getContent());
        if(urls.size() > systemConfig.getMaxPostImageNum()){
            throw new ServiceException("????????????????????????");
        }

        //?????????????????????????????????(?????????????????????????????????)
        List<Long> imageFileIds = new ArrayList<>(urls.size());
        for (String url : urls) {
            FileIdentity imageFileIdentity = fileService.getFileIdentityByUrl(url);
            if(imageFileIdentity == null) continue;  //???????????????

            if(Objects.equals(imageFileIdentity.getUploaderId(), currentUser.getId()) && FileService.POST_IMAGE_FILE.equals(imageFileIdentity.getSource())){
                imageFileIds.add(imageFileIdentity.getId());
            }
        }
        if(!imageFileIds.isEmpty()){
            attachmentMapper.insertAttachments(post.getId(), null, imageFileIds);

            //????????????
            fileService.increaseReferenceCount(imageFileIds, 1);
        }

        //?????????????????????????????????ElasticSearch
        postMessageSender.sendPostAddMessage(post);
        return true;
    }

    @Override
    public PostDto getPost(Long postId) {
        //?????????++
        statisticsService.increasePostViewCount(postId);
        //????????????????????????????????????
        statisticsService.markPostToFreshScore(postId);

        //?????????????????????PostDto
        String postKey = POST_CACHE_KEY_PREFIX + postId;
        String cachedPostJson = redisTemplate.opsForValue().get(postKey);
        if(cachedPostJson != null){
            PostDto cachedPostDto = JSON.parseObject(cachedPostJson, PostDto.class);

            //?????? ????????? ??? ????????????????????????
            setLikeCountAndStatus(cachedPostDto);
            return cachedPostDto;
        }

        //??????????????????????????????????????????
        Post post = postMapper.selectById(postId);
        PostDto postDto = convertToPostDto(post);
        redisTemplate.opsForValue().set(postKey, JSON.toJSONString(postDto), CACHE_EXPIRE_SECONDS, TimeUnit.SECONDS);
        return postDto;
    }

    @Override
    public IPage<PostDto> listPosts(PageQuery pageQuery, Long forumId, Long authorId) {
        LambdaQueryWrapper<Post> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(forumId != null, Post::getForumId, forumId);
        queryWrapper.eq(authorId != null, Post::getAuthorId, authorId);

        IPage<Post> postPage = pageQuery.getIPage(columnsCanSorted, "score", false);
        postPage = postMapper.selectPage(postPage, queryWrapper);
        List<PostDto> postDtos = postPage.getRecords().stream().map(this::convertToPostDto).collect(Collectors.toList());
        return PageUtil.replaceRecords(postPage, postDtos);
    }

    @Override
    public IPage<PostDto> searchPosts(PageQuery pageQuery, String text, Long forumId, Long authorId) {
        IPage<Post> postPage = searchService.searchPosts(pageQuery, text, authorId, forumId);
        List<Post> posts = postPage.getRecords();
        if(posts.isEmpty()) return PageUtil.replaceRecords(postPage, Collections.emptyList());

        //??????likeCount??????
        List<Long> postIds = posts.stream().map(Post::getId).collect(Collectors.toList());
        List<Long> likeCountBatch = postMapper.selectLikeCountBatch(postIds);
        for(int i = 0; i < postIds.size(); i++){
            posts.get(i).setLikeCount(likeCountBatch.get(i));
        }

        //to PostDto
        List<PostDto> postDtos = posts.stream().map(this::convertToPostDto).collect(Collectors.toList());
        return PageUtil.replaceRecords(postPage, postDtos);
    }

    @Override
    public boolean removePost(Long postId) {
        //??????????????????
        //??????????????????-1
        List<Long> attachmentFileIds = attachmentMapper.selectAttachmentFileIdsOfPost(postId);
        if(!attachmentFileIds.isEmpty()){
            fileService.increaseReferenceCount(attachmentFileIds, -1);
        }
        //??????attachment??????
        Attachment attachmentQuery = new Attachment();
        attachmentQuery.setPostId(postId);
        attachmentMapper.delete(new QueryWrapper<>(attachmentQuery));

        //??????????????????
        Comment commentQuery = new Comment();
        commentQuery.setPostId(postId);
        commentMapper.delete(new QueryWrapper<>(commentQuery));

        if(postMapper.deleteById(postId) > 0){
            //?????????????????????????????????ElasticSearch
            postMessageSender.sendPostDeleteMessage(postId);

            //????????????
            redisTemplate.delete(POST_CACHE_KEY_PREFIX+postId);
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

    private PostDto convertToPostDto(Post post){
        if(post == null) return null;
        PostDto postDto = new PostDto();
        BeanUtils.copyProperties(post, postDto);

        //??????????????????
        User user = userService.getUser(post.getAuthorId());
        UserDto userDto = new UserDto();
        BeanUtils.copyProperties(user, userDto);
        postDto.setAuthor(userDto);

        //????????????
        Forum forum = forumService.getById(post.getForumId());
        postDto.setForumName(forum.getName());

        //???????????? / ????????????
        setLikeCountAndStatus(postDto);
        return postDto;
    }

    /**
     * ??????PostDto??? ???????????? ??? ???????????????????????????
     */
    private void setLikeCountAndStatus(PostDto postDto){
        //???????????????????????????
        Long currentUserId = SecurityUtil.isAuthenticated() ? SecurityUtil.getCurrentUser().getId() : null;
        postDto.setLiked(likeService.hasLikedPost(postDto.getId(), currentUserId));

        //??????????????????
        Long likeCount = likeService.getPostLikeCount(postDto.getId());
        if(likeCount != null){
            postDto.setLikeCount(likeCount);
        }
    }
}
