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
import github.qiao712.bbs.domain.entity.*;
import github.qiao712.bbs.exception.ServiceException;
import github.qiao712.bbs.mapper.AttachmentMapper;
import github.qiao712.bbs.mapper.CommentMapper;
import github.qiao712.bbs.mapper.PostMapper;
import github.qiao712.bbs.mq.MessageSender;
import github.qiao712.bbs.mq.MessageType;
import github.qiao712.bbs.service.*;
import github.qiao712.bbs.util.HtmlUtil;
import github.qiao712.bbs.util.PageUtil;
import github.qiao712.bbs.util.SecurityUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
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
    private FileService fileService;
    @Autowired
    private LikeService likeService;
    @Autowired
    private AttachmentMapper attachmentMapper;
    @Autowired
    private CommentMapper commentMapper;
    @Autowired
    private StatisticsService statisticsService;
    @Autowired
    private MessageSender messageSender;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String POST_CACHE_KEY_PREFIX = "post-";
    private static final String POST_LIST_BY_TIME_KEY_PREFIX = "post-list-time-";          //按时间排序的贴子列表缓存
    private static final String POST_LIST_BY_SCORE_KEY_PREFIX = "post-list-score-";        //按热度排序的贴子列表缓存

    @Value("${sys.max-post-image-num}")
    private int maxPostImageNum = 20;                //贴子中图片数量上限
    @Value("${sys.cache.post-cache-valid-time}")
    private int postCacheValidTime = 60;             //单个贴子缓存过期时间 s
    @Value("${sys.cache.post-id-list-cache-max-length}")
    private int cachedPostListMaxLength = 200;       //查询贴子列表时，前n项从缓存中查询

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

        //缓存到按时间排序的列表中
        String key = POST_LIST_BY_TIME_KEY_PREFIX + post.getForumId();
        redisTemplate.opsForZSet().add(key, post.getId().toString(), System.currentTimeMillis());
        redisTemplate.opsForZSet().removeRange(key, 0, -cachedPostListMaxLength-1);  //只缓存前几页，移除旧的ID
        //缓存到按热度排序的表中
        key = POST_LIST_BY_SCORE_KEY_PREFIX + post.getForumId();
        redisTemplate.opsForZSet().add(key, post.getId().toString(), post.getScore());
        redisTemplate.opsForZSet().removeRange(key, 0, -cachedPostListMaxLength-1);  //只缓存前几页，移除旧的ID

        //解析出引用的图片
        List<String> urls = HtmlUtil.getImageUrls(post.getContent());
        if(urls.size() > maxPostImageNum){
            throw new ServiceException(ResultCode.POST_ERROR, "图片数量超出限制");
        }

        //记录该贴子对图片的引用(记录为该贴子的一个附件)
        List<Long> imageFileIds = new ArrayList<>(urls.size());
        for (String url : urls) {
            FileIdentity imageFileIdentity = fileService.getFileIdentityByUrl(url);
            if(imageFileIdentity == null) continue;  //为外部链接

            if(Objects.equals(imageFileIdentity.getUploaderId(), currentUser.getId()) && FileService.POST_IMAGE_FILE.equals(imageFileIdentity.getSource())){
                imageFileIds.add(imageFileIdentity.getId());
            }
        }
        if(!imageFileIds.isEmpty()){
            attachmentMapper.insertAttachments(post.getId(), null, imageFileIds);

            //引用图片
            fileService.increaseReferenceCount(imageFileIds, 1);
        }

        //发布添加事件，以同步至ElasticSearch
        messageSender.sendMessageSync(MessageType.POST_ADD, post.getId().toString(), post);
        return true;
    }

    @Override
    public PostDto getPost(Long postId) {
        //浏览量++
        statisticsService.increasePostViewCount(postId);
        //标记需要更新贴子热度分值
        statisticsService.markPostToFreshScore(postId);

        //先从缓存中取出Post
        String postKey = POST_CACHE_KEY_PREFIX + postId;
        String postDtoJson = redisTemplate.opsForValue().get(postKey);
        if(postDtoJson != null){
            PostDto postDto = JSON.parseObject(postDtoJson, PostDto.class);

            //点赞数量 / 点赞状态
            Long currentUserId = SecurityUtil.isAuthenticated() ? SecurityUtil.getCurrentUser().getId() : null;
            postDto.setLikeCount(likeService.getPostLikeCount(postDto.getId()));
            postDto.setLiked(currentUserId == null ? null : likeService.hasLikedPost(postDto.getId(), currentUserId));  //当前用户是否点赞

            return postDto;
        }

        //若不存在则读数据库，构建缓存
        Post post = postMapper.selectById(postId);
        PostDto postDto = convertToPostDto(post);
        redisTemplate.opsForValue().set(postKey, JSON.toJSONString(postDto), postCacheValidTime, TimeUnit.SECONDS);
        return postDto;
    }

    @Override
    public IPage<PostDto> listPosts(PageQuery pageQuery, Long forumId, Long authorId) {
        int end = pageQuery.getPageNo() * pageQuery.getPageSize();                              //最后的rank(不包括) (pageNo从1开始)
        int start = end - pageQuery.getPageSize();
        boolean isAsc = "asc".equalsIgnoreCase(pageQuery.getOrder());                           //默认降序
        String orderBy = pageQuery.getOrderBy() != null ? pageQuery.getOrderBy() : "score";     //默认按热度排序
        if(!columnsCanSorted.contains(orderBy)){
            throw new ServiceException(ResultCode.INVALID_PARAM, "不允许排序的字段");
        }

        //在按板块查询时，前几页从缓存中查询
        List<PostDto> postDtos = null;
        if(end <= cachedPostListMaxLength && authorId == null && forumId != null && !isAsc){
            //按热度或时间排序
            String key = "create_time".equals(pageQuery.getOrderBy()) ? POST_LIST_BY_TIME_KEY_PREFIX + forumId : POST_LIST_BY_SCORE_KEY_PREFIX + forumId;
            Set<String> postIds = redisTemplate.opsForZSet().reverseRange(key, start, end - 1);

            if(postIds != null && !postIds.isEmpty()){
                postDtos = listPosts(postIds.stream().map(Long::valueOf).collect(Collectors.toList()));
            }else{
                //构建id列表缓存
                LambdaQueryWrapper<Post> postIdQueryWrapper = new LambdaQueryWrapper<>();
                postIdQueryWrapper.select(Post::getId, Post::getCreateTime);
                postIdQueryWrapper.eq(Post::getForumId, forumId);
                postIdQueryWrapper.orderBy(true, false, orderBy.equals("create_time") ? Post::getCreateTime : Post::getScore);
                postIdQueryWrapper.last("limit " + cachedPostListMaxLength);
                List<Post> posts = postMapper.selectList(postIdQueryWrapper);

                if(!posts.isEmpty()){
                    Set<ZSetOperations.TypedTuple<String>> tuples = posts.stream().map(post -> {
                        long timestamp = post.getCreateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();    //贴子创建时间戳
                        return new DefaultTypedTuple<>(post.getId().toString(), (double) timestamp);
                    }).collect(Collectors.toSet());
                    redisTemplate.opsForZSet().add(key, tuples);
                }else{
                    //若为空，添加一个-1进行占位，避免下次再查数据库构建缓存
                    redisTemplate.opsForZSet().add(key, "-1", 0);
                }
            }
        }

        IPage<Post> postPage = pageQuery.getIPage(columnsCanSorted, "score", false);
        LambdaQueryWrapper<Post> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(forumId != null, Post::getForumId, forumId);
        queryWrapper.eq(authorId != null, Post::getAuthorId, authorId);

        if(postDtos != null){
            //查询总数
            Long total = postMapper.selectCount(queryWrapper);
            postPage.setTotal(total);
        }else{
            //未走缓存，则从数据库中查询
            postPage = postMapper.selectPage(postPage, queryWrapper);
            List<Post> posts = postPage.getRecords();
            postDtos = convertToPostDtos(posts);
        }
        return PageUtil.replaceRecords(postPage, postDtos);
    }

    @Override
    public List<PostDto> listPosts(Collection<Long> postIds) {
        if(postIds == null || postIds.isEmpty()) return Collections.emptyList();

        //优先从缓存中取
        List<PostDto> cachedPostDtos = null;
        List<String> postJsons = redisTemplate.opsForValue().multiGet(postIds.stream().map(String::valueOf).collect(Collectors.toList()));
        if(postJsons != null){
            cachedPostDtos = postJsons.stream().filter(Objects::nonNull).map(postJson -> JSON.parseObject(postJson, PostDto.class)).collect(Collectors.toList());
        }

        if(cachedPostDtos != null){
            //刷新/重新设置点赞数 / 用户是否点赞
            Long currentUserId = SecurityUtil.isAuthenticated() ? SecurityUtil.getCurrentUser().getId() : null;
            for (PostDto postDto : cachedPostDtos) {
                postDto.setLikeCount(likeService.getPostLikeCount(postDto.getId()));
                postDto.setLiked(currentUserId == null ? null : likeService.hasLikedPost(postDto.getId(), currentUserId));  //当前用户是否点赞
            }

            //全部获取
            if(cachedPostDtos.size() == postIds.size()) return cachedPostDtos;
        }

        //从数据库中取剩余的部分
        List<Post> uncachedPosts;
        if(cachedPostDtos == null || cachedPostDtos.isEmpty()){
            uncachedPosts = postMapper.selectBatchIds(postIds);
        }else{
            Set<Long> cachedPostIds = cachedPostDtos.stream().map(PostDto::getId).collect(Collectors.toSet());
            List<Long> uncachedPostIds = postIds.stream().filter(cachedPostIds::contains).collect(Collectors.toList());
            uncachedPosts = postMapper.selectBatchIds(uncachedPostIds);
        }
        //聚合其他信息，转为postDtos
        List<PostDto> uncachedPostDtos = convertToPostDtos(uncachedPosts);

        //按postIds的顺序，进行整理
        Map<Long, PostDto> postDtoMap = new HashMap<>(postIds.size());
        if(cachedPostDtos != null){
            for (PostDto postDto : cachedPostDtos) {
                postDtoMap.put(postDto.getId(), postDto);
            }
        }
        if(uncachedPosts != null){
            for (PostDto postDto : uncachedPostDtos) {
                postDtoMap.put(postDto.getId(), postDto);
            }
        }
        List<PostDto> postDtos = postIds.stream().map(postDtoMap::get).filter(Objects::nonNull).collect(Collectors.toList());

        //加入缓存
        //TODO: 异步
        if(uncachedPosts != null){
            for (Post uncachedPost : uncachedPosts) {
                redisTemplate.opsForValue().set(POST_CACHE_KEY_PREFIX+uncachedPost.getId(), JSON.toJSONString(uncachedPost), postCacheValidTime, TimeUnit.SECONDS);
            }
        }
        return postDtos;
    }

    @Override
    public IPage<PostDto> searchPosts(PageQuery pageQuery, String text, Long forumId, Long authorId) {
        IPage<Post> postPage = searchService.searchPosts(pageQuery, text, authorId, forumId);
        List<Post> posts = postPage.getRecords();
        if(posts.isEmpty()) return PageUtil.replaceRecords(postPage, Collections.emptyList());
        //聚合其他信息
        List<PostDto> postDtos = convertToPostDtos(posts);
        return PageUtil.replaceRecords(postPage, postDtos);
    }

    @Override
    public boolean removePost(Long postId) {
        //查出所属板块id，之后操作缓存用
        Post post = postMapper.selectOne(new LambdaQueryWrapper<Post>().eq(Post::getId, postId).select(Post::getForumId));
        if(post == null){
            throw new ServiceException(ResultCode.INVALID_PARAM, "贴子不存在");
        }

        //删除所有附件
        //文件引用计数-1
        List<Long> attachmentFileIds = attachmentMapper.selectAttachmentFileIdsOfPost(postId);
        if(!attachmentFileIds.isEmpty()){
            fileService.increaseReferenceCount(attachmentFileIds, -1);
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
            messageSender.sendMessageSync(MessageType.POST_DELETE, postId.toString(), postId);

            //清除缓存
            redisTemplate.delete(POST_CACHE_KEY_PREFIX+postId);
            redisTemplate.opsForZSet().remove(POST_LIST_BY_SCORE_KEY_PREFIX+post.getForumId(), postId.toString());
            redisTemplate.opsForZSet().remove(POST_LIST_BY_TIME_KEY_PREFIX+post.getForumId(), postId.toString());
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

    /**
     * 批量聚合转换
     */
    private List<PostDto> convertToPostDtos(List<Post> posts){
        if(posts == null || posts.isEmpty()) return Collections.emptyList();
        Long currentUserId = SecurityUtil.isAuthenticated() ? SecurityUtil.getCurrentUser().getId() : null;

        //获取用户信息
        Set<Long> userIds = posts.stream().map(Post::getAuthorId).collect(Collectors.toSet());
        List<UserDto> userDtos = userService.listUsers(userIds);
        Map<Long, UserDto> userMap = userDtos.stream().collect(Collectors.toMap(UserDto::getId, o -> o));

        //获取板块信息
        Set<Long> forumIds = posts.stream().map(Post::getForumId).collect(Collectors.toSet());
        List<Forum> forums = forumService.listByIds(forumIds);
        Map<Long, Forum> forumMap = forums.stream().collect(Collectors.toMap(Forum::getId, o -> o));

        //聚合
        return posts.stream().map(post -> {
            PostDto postDto = new PostDto();
            BeanUtils.copyProperties(post, postDto);
            postDto.setAuthor(userMap.get(post.getAuthorId()));
            postDto.setForumName(forumMap.get(post.getForumId()).getName());
            postDto.setLikeCount(likeService.getPostLikeCount(postDto.getId()));
            postDto.setLiked(currentUserId == null ? null : likeService.hasLikedPost(postDto.getId(), currentUserId));  //当前用户是否点赞
            return postDto;
        }).collect(Collectors.toList());
    }

    /**
     * 聚合其他信息
     */
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
        Long currentUserId = SecurityUtil.isAuthenticated() ? SecurityUtil.getCurrentUser().getId() : null;
        postDto.setLikeCount(likeService.getPostLikeCount(postDto.getId()));
        postDto.setLiked(currentUserId == null ? null : likeService.hasLikedPost(postDto.getId(), currentUserId));  //当前用户是否点赞
        return postDto;
    }
}
