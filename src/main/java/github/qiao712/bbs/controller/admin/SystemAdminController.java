package github.qiao712.bbs.controller.admin;

import github.qiao712.bbs.domain.base.Result;
import github.qiao712.bbs.domain.dto.Statistic;
import github.qiao712.bbs.service.SystemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 系统设置、状态控制
 */
@RestController
@RequestMapping("/api/admin/sys")
public class SystemAdminController {
    @Autowired
    private SystemService systemService;

    @GetMapping("/statistic")
    @PreAuthorize("hasAuthority('admin:statistic:get')")
    public Result<Statistic> getStatistic(){
        return Result.succeed(systemService.getStatistic());
    }
}