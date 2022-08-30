package github.qiao712.bbs.controller.admin;

import github.qiao712.bbs.domain.AddGroup;
import github.qiao712.bbs.domain.base.Result;
import github.qiao712.bbs.domain.dto.Statistic;
import github.qiao712.bbs.domain.entity.Advertisement;
import github.qiao712.bbs.service.SystemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/home-ads")
    @PreAuthorize("hasAuthority('admin:sys:ads:add')")
    public Result<Void> addAdvertising(@Validated(AddGroup.class) @RequestBody Advertisement advertisement){
        return Result.build(systemService.addAdvertisement(advertisement));
    }


}