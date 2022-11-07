package github.qiao712.bbs.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import github.qiao712.bbs.domain.entity.CommentLike;
import github.qiao712.bbs.domain.entity.PostLike;
import github.qiao712.bbs.exception.ServiceException;
import github.qiao712.bbs.mapper.CommentLikeMapper;
import github.qiao712.bbs.mapper.CommentMapper;
import github.qiao712.bbs.mapper.PostLikeMapper;
import github.qiao712.bbs.mapper.PostMapper;
import github.qiao712.bbs.service.LikeService;
import github.qiao712.bbs.service.StatisticsService;
import github.qiao712.bbs.util.DistributedLock;
import github.qiao712.bbs.util.SecurityUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.*;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Slf4j
public class LikeServiceImpl extends ServiceImpl<PostLikeMapper, PostLike> implements LikeService, InitializingBean {
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

    private final static String POST_LIKE_COUNT_TABLE = "post-like-count-table";
    private final static String COMMENT_LIKE_COUNT_TABLE = "comment-like-count-table";

    //确保只有一个节点进行刷新
    private DistributedLock commentLikeCountSyncLock;
    private DistributedLock postLikeCountSyncLock;
    @Override
    public void afterPropertiesSet() throws Exception {
        postLikeCountSyncLock = new DistributedLock("post-like-counts-sync", 100, redisTemplate);
        commentLikeCountSyncLock = new DistributedLock("comment-like-counts-sync", 100, redisTemplate);
    }

    /**
     * 将上面这些hash表拆，每个都解为TABLE_NUM个，将编号以后缀的形式加上。
     * 操作时，通过postId 或 commentId 路由到某个表
     * 将Redis中大的hash表拆成若干个小的，方便同步
     */
    private final static int TABLE_NUM = 10;
    private String getTableName(Long id, String table){
        return table + "-" + id%TABLE_NUM;
    }

    //Post-----------------------------------------------------------
    @Override
    @Transactional
    public void likePost(Long postId, boolean like) {
        Long userId = SecurityUtil.getCurrentUser().getId();

        if(!Boolean.TRUE.equals(postMapper.existsById(postId))){
            throw new ServiceException("贴子不存在");
        }
        if(postLikeMapper.isPostLikedByUser(postId, userId)){
            throw new ServiceException("不可重复点赞");
        }

        //点赞记录
        PostLike postLike = new PostLike();
        postLike.setPostId(postId);
        postLike.setUserId(userId);
        postLikeMapper.insert(postLike);

        //路由到某张表
        String postLikeCountTable = getTableName(postId, POST_LIKE_COUNT_TABLE);

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
        //优先从缓存中获取
        String postLikeCountTable = getTableName(postId, POST_LIKE_COUNT_TABLE); //路由到某张表
        String value = (String) stringRedisTemplate.opsForHash().get(postLikeCountTable, postId.toString());
        return value != null ? Long.parseLong(value) : postMapper.selectLikeCount(postId);
    }

    @Override
    synchronized public void syncPostLikeCount(){
        if(! postLikeCountSyncLock.tryLock()) return;
        long begin = System.nanoTime();
        log.info("开始同步贴子点赞数据");

        final int BATCH_SIZE = 1000;

        for(int i = 0; i < TABLE_NUM; i++) {
            String postLikeCountTable = POST_LIKE_COUNT_TABLE + "-" + i;
            BoundHashOperations<String, String, String> hashOps = stringRedisTemplate.boundHashOps(postLikeCountTable);

            //scan
            try (Cursor<Map.Entry<String, String>> cursor = hashOps.scan(ScanOptions.scanOptions().count(BATCH_SIZE).build())) {
                //收集一批
                while (cursor.hasNext()) {
                    for (int j = 0; j < BATCH_SIZE && cursor.hasNext(); j++) {
                        Map.Entry<String, String> entry = cursor.next();

                        //同步至数据库
                        postMapper.updateLikeCount(Long.parseLong(entry.getKey()), Long.parseLong(entry.getValue()));

                        //在Redis中比较并删除 （若值被该变了则不删能删除）
                        stringRedisTemplate.execute(compareAndDelete, Collections.singletonList(postLikeCountTable), entry.getKey(), entry.getValue());
                    }

                    //将锁续费
                    postLikeCountSyncLock.refresh();
                }
            }
        }

        postLikeCountSyncLock.unlock();
        long end = System.nanoTime();
        log.info("贴子点赞数据同步完成. 耗时:{}ms", (end-begin)/1e6);
    }

    //Comment-----------------------------------------------------------

    @Override
    public void likeComment(Long commentId, boolean like) {
        Long userId = SecurityUtil.getCurrentUser().getId();

        if(!Boolean.TRUE.equals(commentMapper.existsById(commentId))){
            throw new ServiceException("评论不存在");
        }
        if(commentLikeMapper.isCommentLikedByUser(commentId, userId)){
            throw new ServiceException("不可重复点赞");
        }

        //点赞记录
        CommentLike commentLike = new CommentLike();
        commentLike.setCommentId(commentId);
        commentLike.setUserId(userId);
        commentLikeMapper.insert(commentLike);

        //路由到某张表
        String commentLikeCountTable = getTableName(commentId, COMMENT_LIKE_COUNT_TABLE);

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
        String commentLikeCountTable = getTableName(commentId, COMMENT_LIKE_COUNT_TABLE); //路由到某张表
        String value = (String) stringRedisTemplate.opsForHash().get(commentLikeCountTable, commentId.toString());
        return value != null ? Long.parseLong(value) : commentMapper.selectLikeCount(commentId);
    }

    @Override
    synchronized public void syncCommentLikeCount() {
        if(! commentLikeCountSyncLock.tryLock()) return;
        long begin = System.nanoTime();
        log.info("开始同步评论点赞数据");

        final int BATCH_SIZE = 1000;

        for(int i = 0; i < TABLE_NUM; i++) {
            String commentLikeCountTable = COMMENT_LIKE_COUNT_TABLE + "-" + i;
            BoundHashOperations<String, String, String> hashOps = stringRedisTemplate.boundHashOps(commentLikeCountTable);

            //scan
            try (Cursor<Map.Entry<String, String>> cursor = hashOps.scan(ScanOptions.scanOptions().count(BATCH_SIZE).build())) {
                //收集一批
                while (cursor.hasNext()) {
                    for (int j = 0; j < BATCH_SIZE && cursor.hasNext(); j++) {
                        Map.Entry<String, String> entry = cursor.next();

                        //同步至数据库
                        commentMapper.updateLikeCount(Long.parseLong(entry.getKey()), Long.parseLong(entry.getValue()));

                        //在Redis中比较并删除 （若值被该变了则不删能删除）
                        stringRedisTemplate.execute(compareAndDelete, Collections.singletonList(commentLikeCountTable), entry.getKey(), entry.getValue());
                    }

                    //将锁续费
                    commentLikeCountSyncLock.refresh();
                }
            }
        }

        commentLikeCountSyncLock.unlock();
        long end = System.nanoTime();
        log.info("评论点赞数据同步完成. 耗时:{}ms", (end-begin)/1e6);
    }
}
