package github.qiao712.bbs.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import github.qiao712.bbs.domain.base.PageQuery;
import github.qiao712.bbs.domain.dto.UserDto;
import github.qiao712.bbs.domain.entity.Post;

public interface FollowService {
    /**
     * 关注
     */
    boolean follow(Long userId, Long followingId);

    /**
     * 取消关注
     */
    boolean unfollow(Long userId, Long followingId);

    /**
     * 关注列表
     */
    IPage<UserDto> listFollowings(Long userId, PageQuery pageQuery);

    /**
     * 粉丝列表
     */
    IPage<UserDto> listFollowers(Long userId, PageQuery pageQuery);

    /**
     * 获取动态列表
     */
    void listActivities(Long userId);

    /**
     * 将动态推送给关注者
     */
    boolean pushToFollower(Post post);

    /**
     * 用户主动拉取关注的动态
     */
    boolean pullForFollowing(Long userId);
}
