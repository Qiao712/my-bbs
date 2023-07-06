package sdu.addd.qasys.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import sdu.addd.qasys.common.Result;
import sdu.addd.qasys.dto.AuthUser;
import sdu.addd.qasys.dto.UserDto;
import sdu.addd.qasys.service.UserService;
import sdu.addd.qasys.common.PageQuery;
import sdu.addd.qasys.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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

    @PutMapping("/{userId}/avatar")
    @PreAuthorize("isAuthenticated() and #userId == #currentUser.id and hasAuthority('user:set-avatar')")
    public Result<Void> setMyAvatar(@PathVariable("userId") Long userId,
                                      @AuthenticationPrincipal AuthUser currentUser,
                                    String avatarUrl){
        return Result.build(userService.setAvatar(userId, avatarUrl));
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
