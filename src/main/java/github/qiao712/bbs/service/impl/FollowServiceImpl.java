package github.qiao712.bbs.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import github.qiao712.bbs.domain.base.PageQuery;
import github.qiao712.bbs.domain.dto.UserDto;
import github.qiao712.bbs.domain.entity.Follow;
import github.qiao712.bbs.domain.entity.Post;
import github.qiao712.bbs.domain.base.ResultCode;
import github.qiao712.bbs.exception.ServiceException;
import github.qiao712.bbs.mapper.FollowMapper;
import github.qiao712.bbs.service.FollowService;
import github.qiao712.bbs.service.UserService;
import github.qiao712.bbs.util.PageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class FollowServiceImpl implements FollowService {
    @Autowired
    private FollowMapper followMapper;
    @Autowired
    private UserService userService;

    @Override
    @Transactional
    public boolean follow(Long userId, Long followingId) {
        if(Objects.equals(userId, followingId)){
           throw new ServiceException(ResultCode.INVALID_PARAM, "禁止关注自己");
        }

        if(userService.getUsername(followingId) == null){
            throw new ServiceException(ResultCode.INVALID_PARAM, "目标用户不存在");
        }

        Follow follow = new Follow();
        follow.setFollowerId(userId);
        follow.setFollowingId(followingId);
        if(followMapper.exists(new QueryWrapper<>(follow))){
            throw new ServiceException(ResultCode.FOLLOW_ERROR, "已关注");
        }

        return followMapper.insert(follow) > 0;
    }

    @Override
    public boolean unfollow(Long userId, Long followingId) {
        Follow follow = new Follow();
        follow.setFollowerId(userId);
        follow.setFollowingId(followingId);
        if(followMapper.delete(new QueryWrapper<>(follow)) == 0){
            throw new ServiceException(ResultCode.FOLLOW_ERROR, "未关注");
        }

        return true;
    }

    @Override
    public IPage<UserDto> listFollowings(Long userId, PageQuery pageQuery) {
        LambdaQueryWrapper<Follow> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(Follow::getFollowingId);
        queryWrapper.eq(Follow::getFollowerId, userId);
        IPage<Follow> followPage = followMapper.selectPage(pageQuery.getIPage(), queryWrapper);

        List<Long> followingIds = followPage.getRecords().stream().map(Follow::getFollowingId).collect(Collectors.toList());
        List<UserDto> userDtos = userService.listUsers(followingIds);

        return PageUtil.replaceRecords(followPage, userDtos);
    }

    @Override
    public IPage<UserDto> listFollowers(Long userId, PageQuery pageQuery) {
        LambdaQueryWrapper<Follow> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(Follow::getFollowerId);
        queryWrapper.eq(Follow::getFollowingId, userId);
        IPage<Follow> followPage = followMapper.selectPage(pageQuery.getIPage(), queryWrapper);

        List<Long> followerIds = followPage.getRecords().stream().map(Follow::getFollowerId).collect(Collectors.toList());
        List<UserDto> userDtos = userService.listUsers(followerIds);

        return PageUtil.replaceRecords(followPage, userDtos);
    }

    @Override
    public void listActivities(Long userId) {

    }

    @Override
    public boolean pushToFollower(Post post) {
        return false;
    }

    @Override
    public boolean pullForFollowing(Long userId) {
        return false;
    }
}
