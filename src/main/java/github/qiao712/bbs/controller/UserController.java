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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public Result<Void> register(@Validated(AddGroup.class) @RequestBody User user){
        return Result.build(userService.register(user));
    }

    @GetMapping("/self")
    public Result<User> getCurrentUser(@AuthenticationPrincipal AuthUser authUser){
        return Result.succeed(userService.getUser(authUser.getId()));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN') or #userId == #currentUser.id")
    @PostMapping("/{userId}/avatar")
    public Result<Void> setUserAvatar(@PathVariable("userId") Long userId,
                                      @AuthenticationPrincipal AuthUser currentUser,
                                      @RequestPart("file") MultipartFile file){
        return Result.build(userService.setAvatar(userId, file));
    }

    @GetMapping("/{userId}")
    public Result<User> getUser(@PathVariable("userId") Long userId){
        return Result.succeedNotNull(userService.getUser(userId));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping
    public Result<IPage<UserDto>> listUsers(PageQuery pageQuery, UserDto condition){
        return Result.succeed(userService.listUsers(pageQuery, condition));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/{userId}/status/{enable}")
    public Result<Void> setUserStatus(@PathVariable("userId") Long userId, @PathVariable("enable") Boolean enable){
        return Result.build(userService.setUserStatus(userId, enable));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping
    public Result<Void> updateUser(@Validated(UpdateGroup.class) @RequestBody User user){
        return Result.build(userService.updateUser(user));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("/{userId}")
    public Result<Void> removeUser(@PathVariable("userId") Long userId){
        return Result.build(userService.removeUser(userId));
    }
}
