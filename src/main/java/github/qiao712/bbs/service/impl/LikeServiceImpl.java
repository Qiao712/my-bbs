package github.qiao712.bbs.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import github.qiao712.bbs.config.CacheConstant;
import github.qiao712.bbs.domain.base.ResultCode;
import github.qiao712.bbs.domain.entity.CommentLike;
import github.qiao712.bbs.domain.entity.PostLike;
import github.qiao712.bbs.exception.ServiceException;
import github.qiao712.bbs.mapper.CommentLikeMapper;
import github.qiao712.bbs.mapper.CommentMapper;
import github.qiao712.bbs.mapper.PostLikeMapper;
import github.qiao712.bbs.mapper.PostMapper;
import github.qiao712.bbs.service.LikeService;
import github.qiao712.bbs.service.StatisticsService;
import github.qiao712.bbs.util.SecurityUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.*;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

@Service
@Slf4j
public class LikeServiceImpl extends ServiceImpl<PostLikeMapper, PostLike> implements LikeService {
    @Autowired
    private PostLikeMapper postLikeMapper;
    @Autowired
    private PostMapper postMapper;
    @Autowired
    private CommentLikeMapper commentLikeMapper;
    @Autowired
    private CommentMapper commentMapper;
    @Autowired
    private StatisticsService statisticsService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    //若存在则增加
    private final DefaultRedisScript<Long> increaseIfExists = new DefaultRedisScript<>(
        "if redis.call(\"HEXISTS\", KEYS[1], ARGV[1]) == 1\n" +
        "then\n" +
        "    redis.call(\"HINCRBY\", KEYS[1], ARGV[1], ARGV[2])\n" +
        "    return 1\n" +
        "else\n" +
        "    return 0\n" +
        "end", Long.class
    );

    //比较并删除
    private final DefaultRedisScript<Long> compareAndDelete = new DefaultRedisScript<>(
        "if redis.call(\"HGET\", KEYS[1], ARGV[1]) == ARGV[2]\n" +
        "then\n" +
        "    redis.call(\"HDEL\", KEYS[1], ARGV[1])\n" +
        "end", Long.class
    );

    /**
     * 将上面这些hash表拆，每个都解为LIKE_COUNT_TABLE_NUM个，将编号以后缀的形式加上。
     * 操作时，通过postId 或 commentId 路由到某个表
     * 将Redis中大的hash表拆成若干个小的，方便同步
     */
    private String getTableName(Long id, String table){
        return table + "-" + id% CacheConstant.LIKE_COUNT_TABLE_NUM;
    }

    //Post-----------------------------------------------------------
    @Override
    @Transactional
    public void likePost(Long postId, boolean like) {
        Long userId = SecurityUtil.getCurrentUser().getId();

        if(!Boolean.TRUE.equals(postMapper.existsById(postId))){
            throw new ServiceException(ResultCode.INVALID_PARAM, "贴子不存在");
        }
        if(postLikeMapper.isPostLikedByUser(postId, userId)){
            throw new ServiceException(ResultCode.INVALID_PARAM, "不可重复点赞");
        }

        //点赞记录
        PostLike postLike = new PostLike();
        postLike.setPostId(postId);
        postLike.setUserId(userId);
        postLikeMapper.insert(postLike);

        //路由到某张表
        String postLikeCountTable = getTableName(postId, CacheConstant.POST_LIKE_COUNT_TABLE);

        for(int i = 0; i < 10; i++){    //重试次数
            //若存在缓存的值则增/减
            Long result = redisTemplate.execute(increaseIfExists, Collections.singletonList(postLikeCountTable), postId, like ? 1 : -1);
            if(Objects.equals(result, 0L)){
                //失败: 缓存不存在
                //从MySQL中读取并缓存，再循环重试
                Long likeCount = postMapper.selectLikeCount(postId);
                stringRedisTemplate.opsForHash().putIfAbsent(postLikeCountTable, postId.toString(), likeCount.toString());
            }else{
                //成功
                //标记需要更新贴子热度分值
                statisticsService.markPostToFreshScore(postId);
                return;
            }
        }

        throw new RuntimeException("点赞失败");
    }

    @Override
    public boolean hasLikedPost(Long postId, Long userId) {
        return postLikeMapper.isPostLikedByUser(postId, userId);
    }

    @Override
    public Long getPostLikeCount(Long postId) {
        //优先从更新缓存列表中取
        String postLikeCountTable = getTableName(postId, CacheConstant.POST_LIKE_COUNT_TABLE); //路由到某张表
        String value = (String) stringRedisTemplate.opsForHash().get(postLikeCountTable, postId.toString());

        //否则取缓存的数据库中值
        if(value == null){
            value = stringRedisTemplate.opsForValue().get(CacheConstant.POST_LIKE_COUNT+postId);
        }

        //从数据库中取
        if(value == null){
            Long count = postMapper.selectLikeCount(postId);
            stringRedisTemplate.opsForValue().set(CacheConstant.POST_LIKE_COUNT+postId, count.toString(), CacheConstant.POST_CACHE_EXPIRE_TIME);
            return count;
        }

        return Long.parseLong(value);
    }

    @Override
    synchronized public void syncPostLikeCount(){
        long begin = System.nanoTime();
        log.info("开始同步贴子点赞数据");

        final int BATCH_SIZE = 1000;

        for(int i = 0; i < CacheConstant.LIKE_COUNT_TABLE_NUM; i++) {
            String postLikeCountTable = CacheConstant.POST_LIKE_COUNT_TABLE + "-" + i;
            BoundHashOperations<String, String, String> hashOps = stringRedisTemplate.boundHashOps(postLikeCountTable);

            //scan
            try (Cursor<Map.Entry<String, String>> cursor = hashOps.scan(ScanOptions.scanOptions().count(BATCH_SIZE).build())) {
                //收集一批
                while (cursor.hasNext()) {
                    for (int j = 0; j < BATCH_SIZE && cursor.hasNext(); j++) {
                        Map.Entry<String, String> entry = cursor.next();

                        //同步至数据库
                        long postId = Long.parseLong(entry.getKey());
                        long count = Long.parseLong(entry.getValue());
                        postMapper.updateLikeCount(postId, count);

                        //在Redis中比较并删除 （若值被该变了则不删能删除）
                        stringRedisTemplate.execute(compareAndDelete, Collections.singletonList(postLikeCountTable), entry.getKey(), entry.getValue());
                        //删除对数据库中值的缓存
                        stringRedisTemplate.delete(CacheConstant.POST_LIKE_COUNT+ postId);
                    }
                }
            }
        }

        long end = System.nanoTime();
        log.info("贴子点赞数据同步完成. 耗时:{}ms", (end-begin)/1e6);
    }

    //Comment-----------------------------------------------------------

    @Override
    public void likeComment(Long commentId, boolean like) {
        Long userId = SecurityUtil.getCurrentUser().getId();

        if(!Boolean.TRUE.equals(commentMapper.existsById(commentId))){
            throw new ServiceException(ResultCode.INVALID_PARAM, "评论不存在");
        }
        if(commentLikeMapper.isCommentLikedByUser(commentId, userId)){
            throw new ServiceException(ResultCode.INVALID_PARAM, "不可重复点赞");
        }

        //点赞记录
        CommentLike commentLike = new CommentLike();
        commentLike.setCommentId(commentId);
        commentLike.setUserId(userId);
        commentLikeMapper.insert(commentLike);

        //路由到某张表
        String commentLikeCountTable = getTableName(commentId, CacheConstant.COMMENT_LIKE_COUNT_TABLE);

        for(int i = 0; i < 10; i++){    //重试次数
            //若存在缓存的值则增/减
            Long result = redisTemplate.execute(increaseIfExists, Collections.singletonList(commentLikeCountTable), commentId, like ? 1 : -1);
            if(Objects.equals(result, 0L)){
                //失败: 缓存不存在
                //从MySQL中读取并缓存，再循环重试
                Long likeCount = commentMapper.selectLikeCount(commentId);
                stringRedisTemplate.opsForHash().putIfAbsent(commentLikeCountTable, commentId.toString(), likeCount.toString());
            }else{
                //成功
                return;
            }
        }

        throw new RuntimeException("点赞失败");
    }

    @Override
    public boolean hasLikedComment(Long commentId, Long userId) {
        return commentLikeMapper.isCommentLikedByUser(commentId, userId);
    }

    @Override
    public Long getCommentLikeCount(Long commentId) {
        //优先从缓存中获取
        String commentLikeCountTable = getTableName(commentId, CacheConstant.COMMENT_LIKE_COUNT_TABLE); //路由到某张表
        String value = (String) stringRedisTemplate.opsForHash().get(commentLikeCountTable, commentId.toString());

        //否则取缓存的数据库中值
        if(value == null){
            value = stringRedisTemplate.opsForValue().get(CacheConstant.COMMENT_LIKE_COUNT+commentId);
        }

        //从数据库中取
        if(value == null){
            Long count = commentMapper.selectLikeCount(commentId);
            stringRedisTemplate.opsForValue().set(CacheConstant.COMMENT_LIKE_COUNT+commentId, count.toString(), CacheConstant.POST_CACHE_EXPIRE_TIME);
            return count;
        }

        return Long.parseLong(value);
    }

    @Override
    synchronized public void syncCommentLikeCount() {
        long begin = System.nanoTime();
        log.info("开始同步评论点赞数据");

        final int BATCH_SIZE = 1000;

        for(int i = 0; i < CacheConstant.LIKE_COUNT_TABLE_NUM; i++) {
            String commentLikeCountTable = CacheConstant.COMMENT_LIKE_COUNT_TABLE + "-" + i;
            BoundHashOperations<String, String, String> hashOps = stringRedisTemplate.boundHashOps(commentLikeCountTable);

            //scan
            try (Cursor<Map.Entry<String, String>> cursor = hashOps.scan(ScanOptions.scanOptions().count(BATCH_SIZE).build())) {
                //收集一批
                while (cursor.hasNext()) {
                    for (int j = 0; j < BATCH_SIZE && cursor.hasNext(); j++) {
                        Map.Entry<String, String> entry = cursor.next();

                        //同步至数据库
                        long commentId = Long.parseLong(entry.getKey());
                        long count = Long.parseLong(entry.getValue());
                        commentMapper.updateLikeCount(commentId, count);

                        //在Redis中比较并删除 （若值被该变了则不删能删除）
                        stringRedisTemplate.execute(compareAndDelete, Collections.singletonList(commentLikeCountTable), entry.getKey(), entry.getValue());
                        //删除对数据库中值的缓存
                        stringRedisTemplate.delete(CacheConstant.COMMENT_LIKE_COUNT+commentId);
                    }
                }
            }
        }

        long end = System.nanoTime();
        log.info("评论点赞数据同步完成. 耗时:{}ms", (end-begin)/1e6);
    }
}
