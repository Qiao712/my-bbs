package github.qiao712.bbs.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import github.qiao712.bbs.domain.AddGroup;
import github.qiao712.bbs.domain.UpdateGroup;
import github.qiao712.bbs.domain.base.PageQuery;
import github.qiao712.bbs.domain.base.Result;
import github.qiao712.bbs.domain.entity.Authority;
import github.qiao712.bbs.domain.entity.Role;
import github.qiao712.bbs.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/roles")
public class RoleAdminController {
    @Autowired
    private RoleService roleService;

    @GetMapping
    @PreAuthorize("hasAuthority('admin:role:list')")
    public Result<List<Role>> listRoles(){
        return Result.succeed(roleService.listRoles());
    }

    @GetMapping("/{roleId}")
    @PreAuthorize("hasAuthority('admin:role:get')")
    public Result<Role> getRole(@PathVariable Long roleId){
        return Result.succeedNotNull(roleService.getRole(roleId));
    }

    @PutMapping
    @PreAuthorize("hasAuthority('admin:role:update')")
    public Result<Void> updateRole(@Validated(UpdateGroup.class) @RequestBody Role role){
        return Result.build(roleService.updateRole(role));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('admin:role:add')")
    public Result<Void> addRole(@Validated(AddGroup.class) @RequestBody Role role){
        return Result.build(roleService.addRole(role));
    }

    @GetMapping("/authorities")
    @PreAuthorize("hasAuthority('admin:authority:list')")
    public Result<List<Authority>> getAuthorities(){
        return Result.succeedNotNull(roleService.listAuthorities());
    }

    @PutMapping("/authorities")
    @PreAuthorize("hasAuthority('admin:authority:update')")
    public Result<Void> updateAuthority(@Validated(UpdateGroup.class) @RequestBody Authority authority){
        return Result.build(roleService.updateAuthority(authority));
    }
}
