package github.qiao712.bbs.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import github.qiao712.bbs.config.CacheConstant;
import github.qiao712.bbs.domain.base.ResultCode;
import github.qiao712.bbs.domain.dto.PostDto;
import github.qiao712.bbs.domain.entity.Feed;
import github.qiao712.bbs.domain.entity.Follow;
import github.qiao712.bbs.domain.entity.Post;
import github.qiao712.bbs.exception.ServiceException;
import github.qiao712.bbs.mapper.FeedMapper;
import github.qiao712.bbs.mapper.PostMapper;
import github.qiao712.bbs.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class FeedServiceImpl extends ServiceImpl<FeedMapper, Feed> implements FeedService {
    @Autowired
    private FollowService followService;
    @Autowired
    private StatisticsService statisticsService;
    @Autowired
    private PostMapper postMapper;
    @Autowired
    private PostService postService;
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public List<PostDto> listFeeds(Long followerId, Long after, Integer size) {
        if(size > 20){
            throw new ServiceException(ResultCode.INVALID_PARAM, "获取条数过多");
        }
        if(size <= 0){
            throw new ServiceException(ResultCode.INVALID_PARAM, "size参数非法");
        }

        LambdaQueryWrapper<Feed> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(Feed::getPostId);
        queryWrapper.eq(Feed::getFollowerId, followerId);
        queryWrapper.eq(Feed::getTime, after);
        queryWrapper.orderByDesc(Feed::getTime);
        queryWrapper.last("limit " + size);
        List<Feed> feeds = baseMapper.selectList(queryWrapper);
        List<Long> postIds = feeds.stream().map(Feed::getPostId).collect(Collectors.toList());
        return postService.listPosts(postIds);
    }

    @Override
    public void pushFeed(Post post) {
        //缓存到zset
        String key = CacheConstant.OUTBOX_KEY_PREFIX + ":" + post.getAuthorId();
        redisTemplate.opsForZSet().add(key, post.getId().toString(), post.getCreateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());

        //实现zset中个键的过期时间
        //重置zset整体过期时间
        redisTemplate.expire(key, CacheConstant.OUTBOX_CACHE_EXPIRE_TIME, TimeUnit.MILLISECONDS);
        //删除zset中已经过期的键
        long now = System.currentTimeMillis();
        redisTemplate.opsForZSet().removeRangeByScore(key, 0, now - CacheConstant.OUTBOX_CACHE_EXPIRE_TIME);

        //推送给活跃用户
        LambdaQueryWrapper<Follow> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(Follow::getFollowerId);
        queryWrapper.eq(Follow::getFolloweeId, post.getAuthorId());
        List<Follow> followers = followService.list(queryWrapper);
        List<Long> followerIds = followers.stream().map(Follow::getFollowerId).collect(Collectors.toList());

        for (Long followerId : followerIds) {
            //判断若为活跃用户，则立即推送
            if(statisticsService.isActiveUser(followerId)){
                updateFeed(followerId, post.getAuthorId());
            }
        }
    }

    private void updateFeed(Long followerId, Long followeeId) {
        LambdaQueryWrapper<Follow> followQueryWrapper = new LambdaQueryWrapper<Follow>().eq(Follow::getFollowerId, followerId).eq(Follow::getFolloweeId, followeeId);
        Follow follow = followService.getOne(followQueryWrapper);
        if(follow == null){
            throw new ServiceException(ResultCode.INVALID_PARAM, "不存在关注关系");
        }

        //拉取follow.lastFeedTime之后的feed
        List<Feed> feeds = new ArrayList<>();
        LocalDateTime lastFeedTime = follow.getLastFeedTime();
        long lastFeedTimestamp = lastFeedTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

        //从Redis中的outbox中取 时间戳before前的postId, (旧->新)
        String key = CacheConstant.OUTBOX_KEY_PREFIX + ":" + followeeId;
        Set<ZSetOperations.TypedTuple<String>> postIdsWithTime = redisTemplate.opsForZSet().rangeByScoreWithScores(key, lastFeedTimestamp, Double.MAX_VALUE);
        if(postIdsWithTime != null){
            for (ZSetOperations.TypedTuple<String> postIdWithTime : postIdsWithTime) {
                Feed feed = new Feed();
                feed.setPostId(Long.valueOf(postIdWithTime.getValue()));
                feed.setTime(new Timestamp(postIdWithTime.getScore().longValue()));
                feed.setFollowerId(followerId);
                feed.setFolloweeId(followeeId);
                feeds.add(feed);
            }
        }

        //判断是否需要从数据库中读取feed: 若从zset中未拉取到列表 或 列表位于全部zset的开头
        Long firstRank = null;
        Timestamp firstTime = null;
        if(!feeds.isEmpty()){
            String firstId = feeds.get(0).getPostId().toString();
            firstRank = redisTemplate.opsForZSet().rank(key, firstId);
            firstTime = feeds.get(0).getTime();
        }

        if(firstRank == null || 0L < firstRank){
            LambdaQueryWrapper<Post> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.select(Post::getId)
                    .eq(Post::getAuthorId, followeeId)
                    .ge(Post::getCreateTime, lastFeedTime)
                    .le(firstTime != null, Post::getCreateTime, firstTime);

            List<Post> posts = postMapper.selectList(queryWrapper);
            for (Post post : posts) {
                Feed feed = new Feed();
                feed.setPostId(post.getId());
                feed.setFollowerId(followerId);
                feed.setFolloweeId(followeeId);
                feed.setTime(Timestamp.valueOf(post.getCreateTime()));
                feeds.add(feed);
            }
        }
    }
}
