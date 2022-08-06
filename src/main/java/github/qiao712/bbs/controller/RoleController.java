package github.qiao712.bbs.controller;

import github.qiao712.bbs.domain.base.Result;
import github.qiao712.bbs.domain.entity.Role;
import github.qiao712.bbs.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
public class RoleController {
    @Autowired
    private RoleService roleService;

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping
    public Result<List<Role>> listRoles(){
        return Result.succeed(roleService.list());
    }
}
