package github.qiao712.bbs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import github.qiao712.bbs.domain.entity.Feed;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;

@Mapper
public interface FeedMapper extends BaseMapper<Feed> {
    /**
     * 将贴子发布动态推送至所有关注者
     */
    Integer pushActivities(Long postId, LocalDateTime sendTime, Long authorId);

    /**
     * 从 粉丝数量超过阈值 的关注中拉取
     */
    Integer pullActivities(Long userId, int threshold);

    /**
     * 全量拉取某用户的贴子动态
     */
    Integer pullActivitiesFromUser(Long followerId, Long followingId);
}
