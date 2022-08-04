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
    public Result<UserDto> getCurrentUser(@AuthenticationPrincipal AuthUser authUser){
        return Result.succeed(userService.getUser(authUser.getId()));
    }

    @PostMapping("/avatar")
    public Result<Void> setUserAvatar(@RequestPart("file") MultipartFile file, @AuthenticationPrincipal AuthUser authUser){
        return Result.build(userService.setAvatar(authUser.getId(), file));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping
    public Result<IPage<UserDto>> getUsers(PageQuery pageQuery, UserDto condition){
        return Result.succeed(userService.listUsers(pageQuery, condition));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/{userId}/status/{enable}")
    public Result<Void> setUserStatus(@PathVariable("userId") Long userId, @PathVariable("enable") Boolean enable){
        return Result.build(userService.setUserStatus(userId, enable));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/")
    public Result<Void> updateUser(@Validated(UpdateGroup.class) User user){
        return Result.build(userService.updateById(user));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("/{userId}")
    public Result<Void> deleteUser(@PathVariable("userId") Long userId){
        return Result.build(userService.removeById(userId));
    }
}
