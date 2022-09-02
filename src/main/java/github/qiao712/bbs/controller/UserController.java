package github.qiao712.bbs.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import github.qiao712.bbs.domain.AddGroup;
import github.qiao712.bbs.domain.UpdateGroup;
import github.qiao712.bbs.domain.base.PageQuery;
import github.qiao712.bbs.domain.base.Result;
import github.qiao712.bbs.domain.dto.AuthUser;
import github.qiao712.bbs.domain.dto.UserDto;
import github.qiao712.bbs.domain.entity.User;
import github.qiao712.bbs.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping("/register")
    @PreAuthorize("hasAuthority('user:register')")
    public Result<Void> register(@RequestBody User user){
        return Result.build(userService.register(user));
    }

    @GetMapping("/self")
    @PreAuthorize("isAuthenticated()")
    public Result<User> getCurrentUser(@AuthenticationPrincipal AuthUser authUser){
        return Result.succeed(userService.getUser(authUser.getId()));
    }

    @PostMapping("/{userId}/avatar")
    @PreAuthorize("isAuthenticated() and #userId == #currentUser.id and hasAuthority('user:set-avatar')")
    public Result<Void> setMyAvatar(@PathVariable("userId") Long userId,
                                      @AuthenticationPrincipal AuthUser currentUser,
                                      @RequestPart("file") MultipartFile file){
        return Result.build(userService.setAvatar(userId, file));
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasAuthority('user:get')")
    public Result<User> getUser(@PathVariable("userId") Long userId){
        return Result.succeedNotNull(userService.getUser(userId));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('user:lists')")
    public Result<IPage<UserDto>> listUsers(PageQuery pageQuery, UserDto condition){
        return Result.succeed(userService.listUsers(pageQuery, condition));
    }
}
