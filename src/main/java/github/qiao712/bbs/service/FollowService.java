package github.qiao712.bbs.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import github.qiao712.bbs.domain.base.PageQuery;
import github.qiao712.bbs.domain.dto.PostDto;
import github.qiao712.bbs.domain.dto.UserDto;
import github.qiao712.bbs.domain.entity.Follow;
import github.qiao712.bbs.domain.entity.Post;
import org.springframework.transaction.annotation.Transactional;

public interface FollowService extends IService<Follow> {
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
}
