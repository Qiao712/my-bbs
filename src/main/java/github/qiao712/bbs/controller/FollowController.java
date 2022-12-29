package github.qiao712.bbs.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import github.qiao712.bbs.domain.base.PageQuery;
import github.qiao712.bbs.domain.base.Result;
import github.qiao712.bbs.domain.dto.AuthUser;
import github.qiao712.bbs.domain.dto.UserDto;
import github.qiao712.bbs.service.FollowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/follows")
public class FollowController {
    @Autowired
    private FollowService followService;

    @PostMapping("/follow/{followingId}")
    public Result<Void> follow(@PathVariable("followingId") Long followingId, @AuthenticationPrincipal AuthUser authUser){
        return Result.build(followService.follow(authUser.getId(), followingId));
    }

    @PostMapping("/unfollow/{followingId}")
    public Result<Void> unfollow(@PathVariable("followingId") Long followingId, @AuthenticationPrincipal AuthUser authUser){
        return Result.build(followService.unfollow(authUser.getId(), followingId));
    }

    @GetMapping("/followings")
    public Result<IPage<UserDto>> listFollowings(PageQuery pageQuery, @AuthenticationPrincipal AuthUser authUser){
        return Result.succeedNotNull(followService.listFollowings(authUser.getId(), pageQuery));
    }

    @GetMapping("/followers")
    public Result<IPage<UserDto>> listFollowers(PageQuery pageQuery, @AuthenticationPrincipal AuthUser authUser){
        return Result.succeedNotNull(followService.listFollowers(authUser.getId(), pageQuery));
    }
}
