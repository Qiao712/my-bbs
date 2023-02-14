package github.qiao712.bbs.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import github.qiao712.bbs.config.Constant;
import github.qiao712.bbs.domain.base.ResultCode;
import github.qiao712.bbs.domain.entity.Forum;
import github.qiao712.bbs.domain.entity.Post;
import github.qiao712.bbs.exception.ServiceException;
import github.qiao712.bbs.mapper.PostMapper;
import github.qiao712.bbs.service.ForumService;
import github.qiao712.bbs.service.LikeService;
import github.qiao712.bbs.service.StatisticsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.*;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class StatisticsServiceImpl implements StatisticsService {
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private PostMapper postMapper;
    @Autowired
    private ForumService forumService;
    @Autowired
    private LikeService likeService;

    @Override
    public void increasePostViewCount(long postId) {
        try{
            redisTemplate.opsForHash().increment(Constant.POST_VIEW_COUNT_TABLE, String.valueOf(postId), 1);
        }catch (RuntimeException e){
            //捕获所有异常防止其影响贴子的获取
            log.error("贴子浏览量增加失败", e);
        }
    }

    @Override
    public void markPostToFreshScore(long postId) {
        try {
            redisTemplate.opsForSet().add(Constant.POST_SCORE_REFRESH_TABLE, String.valueOf(postId));
        }catch (RuntimeException e){
            log.error("标记贴子热度分值需要更新失败", e);
        }
    }

    @Override
    public List<Long> listPostViewCounts(List<Long> postIds) {
        List<Long> viewCounts = postMapper.selectViewCountBatch(postIds);
        if(viewCounts.size() != postIds.size()){
            throw new ServiceException(ResultCode.INVALID_PARAM, "包含无效PostId");
        }

        //redis中的一部分
        List<String> postKeys = new ArrayList<>(postIds.size());
        for (Long postId : postIds) {
            postKeys.add(postId.toString());
        }
        HashOperations<String, String, String> hashOps = redisTemplate.opsForHash();
        List<String> viewCountDeltas = hashOps.multiGet(Constant.POST_VIEW_COUNT_TABLE, postKeys);
        if(viewCounts.size() != viewCountDeltas.size()){
            throw new RuntimeException("从Redis中获取浏览量失败");
        }

        //加上redis中缓存的增量
        for(int i = 0; i < viewCounts.size(); i++){
            if(viewCountDeltas.get(i) != null){
                viewCounts.set(i, viewCounts.get(i) + Long.parseLong(viewCountDeltas.get(i)));
            }
        }

        return viewCounts;
    }

    @Override
    public void syncPostViewCount() {
        log.info("同步贴子浏览量: 开始");

        final int BATCH_SIZE = 1000;    //收集多少条插入一次数据库
        List<Map.Entry<String, String>> entries = new ArrayList<>(BATCH_SIZE);

        BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(Constant.POST_VIEW_COUNT_TABLE);
        try(Cursor<Map.Entry<String, String>> cursor = hashOps.scan(ScanOptions.scanOptions().count(BATCH_SIZE).build())){  //scan
            //收集一批
            while(cursor.hasNext()){
                for(int i = 0; i < BATCH_SIZE && cursor.hasNext(); i++){
                    Map.Entry<String, String> entry = cursor.next();
                    entries.add(entry);
                }

                //从Redis中删除
                Object[] keys = entries.stream().map(Map.Entry::getKey).toArray();
                hashOps.delete(keys);

                //同步至数据库
                for (Map.Entry<String, String> entry : entries) {
                    postMapper.increaseViewCount(Long.parseLong(entry.getKey()), Long.parseLong(entry.getValue()));
                }
                entries.clear();
            }
        }

        log.info("同步贴子浏览量: 完成");
    }

    @Override
    public void refreshPostScores(){
        log.info("贴子热度刷新: 开始");

        final int BATCH_SIZE = 1000;
        List<Long> postIds = new ArrayList<>(BATCH_SIZE);
        BoundSetOperations<String, String> setOps = redisTemplate.boundSetOps(Constant.POST_SCORE_REFRESH_TABLE);

        try(Cursor<String> cursor = setOps.scan(ScanOptions.scanOptions().count(BATCH_SIZE).build())){
            if(cursor == null) {
                log.info("贴子热度刷新: Redis游标获取失败");
                return;
            }

            while(cursor.hasNext()){
                //收集一批
                for(int i = 0; cursor.hasNext() && i < BATCH_SIZE; i++){
                    postIds.add(Long.parseLong(cursor.next()));
                }

                //从Redis中删除
                Object[] keys = postIds.stream().map(Object::toString).toArray();
                setOps.remove(keys);

                //重新计算并保存贴子热度
                if(!postIds.isEmpty()){
                    updatePostScore(postIds);
                }
                postIds.clear();
            }
        }

        //重置按热度缓存的热度列表
        List<Forum> forums = forumService.list();
        for (Forum forum : forums) {
            redisTemplate.delete(Constant.POST_LIST_BY_SCORE_KEY_PREFIX + forum.getId());
        }

        log.info("贴子热度刷新: 完成");
    }

    @Override
    public Long computePostScore(long likeCount, long commentCount, long viewCount, LocalDateTime createTime){
        //10个赞可以相当于1分钟
        //2个评论可以相当于1分钟
        return likeCount/10L + commentCount/2L + viewCount/30L + Duration.between(Constant.POST_EPOCH, createTime).toMinutes();
    }

    /**
     * 保存缓存的贴子浏览量至数据库后，需要刷新热度分值的贴子的热度分值
     * 供定时任务调用
     */
    public void syncPostViewCountAndScore(){
        syncPostViewCount();
        refreshPostScores();
    }

    /**
     * 计算贴子热度分数，并更新
     */
    private void updatePostScore(List<Long> postIds) {
        LambdaQueryWrapper<Post> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(Post::getId, Post::getCreateTime, Post::getLikeCount, Post::getViewCount, Post::getCommentCount);
        queryWrapper.in(Post::getId, postIds);
        List<Post> posts = postMapper.selectList(queryWrapper);

        for (Post post : posts) {
            Long score = computePostScore(likeService.getPostLikeCount(post.getId()), post.getCommentCount(), post.getViewCount(), post.getCreateTime());
            postMapper.updateScore(post.getId(), score);
        }
    }

    @Override
    public void markUserActive(Long userId) {
        redisTemplate.opsForValue().setBit(Constant.USER_ACTIVE_BITMAP, userId, true);
    }

    @Override
    public boolean isActiveUser(Long userId) {
        Boolean flag = redisTemplate.opsForValue().getBit(Constant.USER_ACTIVE_BITMAP, userId);
        return flag != null && flag;
    }
}
