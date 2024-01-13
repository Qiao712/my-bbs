package qiao.qasys.controller.admin;

import qiao.qasys.entity.User;
import qiao.qasys.common.UpdateGroup;
import qiao.qasys.common.Result;
import qiao.qasys.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
public class UserAdminController {
    @Autowired
    private UserService userService;

    @PutMapping("/{userId}/status/{enable}")
    @PreAuthorize("hasAuthority('admin:user:status:update')")
    public Result<Void> setUserStatus(@PathVariable("userId") Long userId, @PathVariable("enable") Boolean enable){
        return Result.build(userService.setUserStatus(userId, enable));
    }

    @PutMapping
    @PreAuthorize("hasAuthority('admin:user:update')")
    public Result<Void> updateUser(@Validated(UpdateGroup.class) @RequestBody User user){
        return Result.build(userService.updateUser(user));
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasAuthority('admin:user::update')")
    public Result<Void> removeUser(@PathVariable("userId") Long userId){
        return Result.build(userService.removeUser(userId));
    }
}
