package github.qiao712.bbs.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import github.qiao712.bbs.domain.entity.QuestionLike;
import github.qiao712.bbs.exception.ServiceException;
import github.qiao712.bbs.mapper.AnswerLikeMapper;
import github.qiao712.bbs.mapper.CommentMapper;
import github.qiao712.bbs.mapper.QuestionLikeMapper;
import github.qiao712.bbs.mapper.QuestionMapper;
import github.qiao712.bbs.service.LikeService;
import github.qiao712.bbs.service.StatisticsService;
import github.qiao712.bbs.util.SecurityUtil;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class LikeServiceImpl extends ServiceImpl<QuestionLikeMapper, QuestionLike> implements LikeService {
    @Autowired
    private QuestionLikeMapper questionLikeMapper;
    @Autowired
    private QuestionMapper questionMapper;
    @Autowired
    private StatisticsService statisticsService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private final static String POST_LIKE_COUNT_TABLE = "post-like-counts";
    private final static String POST_LIKE_RECORD_TABLE = "post-like-records";
    private final static String COMMENT_LIKE_COUNT_TABLE = "comment-like-counts";
    private final static String COMMENT_LIKE_RECORD_TABLE = "comment-like-records";
    private final static String LIKED = "1";
    private final static String UNLIKED = "0";

    /**
     * 将上面这些hash表拆，每个都解为TABLE_NUM个，将编号以后缀的形式加上。
     * 操作时，通过postId 或 commentId 路由到某个表
     * 将Redis中大的hash表拆成若干个小的，方便同步
     */
    private final static int TABLE_NUM = 10;
    private String getTableName(Long id, String table){
        return table + "-" + id%TABLE_NUM;
    }

    @Override
    public void likePost(Long postId, boolean like) {
        Long userId = SecurityUtil.getCurrentUser().getId();

        if(!Boolean.TRUE.equals(questionMapper.existsById(postId))){
            throw new ServiceException("贴子不存在");
        }

        String likeRecordKey = userId + ":" + postId;
        String postIdKey = postId.toString();
        HashOperations<String, Object, Object> hashOps = redisTemplate.opsForHash();

        //路由到某张表
        String postLikeCountTable = getTableName(postId, POST_LIKE_COUNT_TABLE);
        String postLikeRecordTable = getTableName(postId, POST_LIKE_RECORD_TABLE);

        //判断是否已点赞
        String likeStatus = (String) hashOps.get(postLikeRecordTable, likeRecordKey);
        boolean liked = likeStatus != null ? LIKED.equals(likeStatus) : questionLikeMapper.isQuestionLikedByUser(postId, userId);
        if(liked && like){
            throw new ServiceException("不可重复点赞");
        }
        if(!liked && !like){
            throw new ServiceException("未点赞");
        }

        //检查缓存，若不存在则缓存
        if(! Boolean.TRUE.equals(hashOps.hasKey(postLikeCountTable, postIdKey))){
            Long likeCount = questionMapper.selectLikeCount(postId);
            hashOps.putIfAbsent(postLikeCountTable, postIdKey, likeCount.toString()); //使用If Absent 防止多次设置缓存覆盖 已经+1的正确缓存
        }

        //标记并累加
        hashOps.put(postLikeRecordTable, likeRecordKey, like ? LIKED : UNLIKED);
        hashOps.increment(postLikeCountTable, postIdKey, like ? 1 : -1);

        //标记需要更新贴子热度分值
        statisticsService.markPostToFreshScore(postId);
    }

    @Override
    public boolean hasLikedPost(Long postId, Long userId) {
        String likeRecordKey = userId + ":" + postId;
        String postLikeRecordTable = getTableName(postId, POST_LIKE_RECORD_TABLE);

        //优先比较缓存中的新值
        String likeStatus = (String) redisTemplate.opsForHash().get(postLikeRecordTable, likeRecordKey);
        return likeStatus != null ? LIKED.equals(likeStatus) : questionLikeMapper.isQuestionLikedByUser(postId, userId);
    }

    @Override
    public Long getPostLikeCountFromCache(Long postId) {
        String postLikeCountTable = getTableName(postId, POST_LIKE_COUNT_TABLE);
        Object value = redisTemplate.opsForHash().get(postLikeCountTable, postId.toString());
        return value != null ? Long.parseLong((String) value) : null;
    }

    @Override
    public void likeComment(Long commentId, boolean like) {
        //TODO
    }

    @Override
    public boolean hasLikedComment(Long commentId, Long userId) {
        //TODO:
        return true;
    }

    @Override
    public Long getCommentLikeCountFromCache(Long commentId) {
        Object value = redisTemplate.opsForHash().get(COMMENT_LIKE_COUNT_TABLE, commentId.toString());
        return value != null ? Long.parseLong((String) value) : null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void syncPostLikeCount(){
        long begin = System.nanoTime();
        log.info("开始同步贴子点赞数据");

        //同步贴子点赞量
        for(int i = 0; i < TABLE_NUM; i++){
            String postLikeCountTable = POST_LIKE_COUNT_TABLE + "-" + i;
            String postLikeRecordTable = POST_LIKE_RECORD_TABLE + "-" + i;

            //在一个事务中获取并删除，避免多个进程同时开始同步该块。并且不会干扰新计数的开始。
            List<Object> result = redisTemplate.execute(new SessionCallback<List<Object>>() {
                @Override
                public List<Object> execute(@NotNull RedisOperations operations) throws DataAccessException {
                    operations.multi();
                    operations.opsForHash().entries(postLikeCountTable);
                    operations.opsForHash().entries(postLikeRecordTable);
                    operations.unlink(postLikeCountTable);
                    operations.unlink(postLikeRecordTable);
                    return operations.exec();
                }
            });
            Map<String, String> postLikeCounts = (Map<String, String>) result.get(0);
            Map<String, String> postLikeRecords = (Map<String, String>) result.get(1);
            log.info("同步 {} 至数据库(size: {})", postLikeCountTable, postLikeCounts.size());
            log.info("同步 {} 至数据库(size: {})", postLikeRecordTable, postLikeRecords.size());

            //同步至点赞数量数据库
            postLikeCounts.forEach((key, value)->{
                questionMapper.updateLikeCount(Long.parseLong(key), Long.parseLong(value));
            });

            //同步用户点赞记录至数据库
            List<QuestionLike> questionLikesToInsert = new ArrayList<>();
            List<QuestionLike> questionLikesToDelete = new ArrayList<>();
            postLikeRecords.forEach((key, value)->{
                String[] split = key.split(":");
                QuestionLike questionLike = new QuestionLike();
                questionLike.setUserId(Long.parseLong(split[0]));
                questionLike.setQuestionId(Long.parseLong(split[1]));

                if(LIKED.equals(value)){
                    questionLikesToInsert.add(questionLike);
                }else if(UNLIKED.equals(value)){
                    questionLikesToDelete.add(questionLike);
                }
            });
            if(!questionLikesToInsert.isEmpty()){
                questionLikeMapper.insertQuestionLikes(questionLikesToInsert);
            }
            if(!questionLikesToDelete.isEmpty()){
                questionLikeMapper.deleteQuestionLikes(questionLikesToDelete);
            }
        }

        long end = System.nanoTime();
        log.info("贴子点赞数据同步完成. 耗时:{}ms", (end-begin)/1e6);
    }

    @Override
    public void syncCommentLikeCount() {
        //TODO
    }
}
