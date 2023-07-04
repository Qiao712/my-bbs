package github.qiao712.bbs.service.impl;

import com.alibaba.fastjson.JSON;
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
import github.qiao712.bbs.domain.entity.Comment;
import github.qiao712.bbs.domain.entity.Forum;
import github.qiao712.bbs.domain.entity.Post;
import github.qiao712.bbs.domain.entity.User;
import github.qiao712.bbs.exception.ServiceException;
import github.qiao712.bbs.mapper.CommentMapper;
import github.qiao712.bbs.mapper.PostMapper;
import github.qiao712.bbs.service.*;
import github.qiao712.bbs.util.HtmlUtil;
import github.qiao712.bbs.util.PageUtil;
import github.qiao712.bbs.util.SecurityUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
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
    private LikeService likeService;
    @Autowired
    private CommentMapper commentMapper;
    @Autowired
    private SystemConfig systemConfig;
    @Autowired
    private StatisticsService statisticsService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private final String POST_CACHE_KEY_PREFIX = "post-";
    private final int CACHE_EXPIRE_SECONDS = 60;            //缓存过期时间 60s

    //Post中允许排序的列
    private final Set<String> columnsCanSorted = new HashSet<>(Arrays.asList("create_time", "score"));

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
            throw new ServiceException(ResultCode.POST_ERROR, "图片数量超出限制");
        }

        //发布添加事件，以同步至ElasticSearch
        try {
            searchService.savePost(post);
        } catch (IOException e) {
            log.error("储存至ElasticSearch失败");
        }
        return true;
    }

    @Override
    public PostDto getPost(Long postId) {
        //浏览量++
        statisticsService.increasePostViewCount(postId);
        //标记需要更新贴子热度分值
        statisticsService.markPostToFreshScore(postId);

        //先从缓存中取出PostDto
        String postKey = POST_CACHE_KEY_PREFIX + postId;
        String cachedPostJson = redisTemplate.opsForValue().get(postKey);
        if(cachedPostJson != null){
            PostDto cachedPostDto = JSON.parseObject(cachedPostJson, PostDto.class);

            //填充 点赞数 和 当前用户点赞状态
            setLikeCountAndStatus(cachedPostDto);
            return cachedPostDto;
        }

        //若不存在则读数据库，构建缓存
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

        //设置likeCount字段
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
        //删除所有评论
        Comment commentQuery = new Comment();
        commentQuery.setPostId(postId);
        commentMapper.delete(new QueryWrapper<>(commentQuery));

        if(postMapper.deleteById(postId) > 0){
            //发布删除事件，以同步至ElasticSearch
            try {
                searchService.removePost(postId);
            } catch (IOException e) {
                log.error("ES中删除失败");
            }

            //清除缓存
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

        //作者用户信息
        User user = userService.getUser(post.getAuthorId());
        UserDto userDto = new UserDto();
        BeanUtils.copyProperties(user, userDto);
        postDto.setAuthor(userDto);

        //板块名称
        Forum forum = forumService.getById(post.getForumId());
        postDto.setForumName(forum.getName());

        //点赞数量 / 点赞状态
        setLikeCountAndStatus(postDto);
        return postDto;
    }

    /**
     * 填充PostDto的 点赞数量 和 当前用户的点赞状态
     */
    private void setLikeCountAndStatus(PostDto postDto){
        //当前用户是否已点赞
        Long currentUserId = SecurityUtil.isAuthenticated() ? SecurityUtil.getCurrentUser().getId() : null;
        postDto.setLiked(likeService.hasLikedPost(postDto.getId(), currentUserId));

        //查询点赞数量
        Long likeCount = likeService.getPostLikeCount(postDto.getId());
        if(likeCount != null){
            postDto.setLikeCount(likeCount);
        }
    }
}
