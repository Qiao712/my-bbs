package github.qiao712.bbs.controller.admin;

import github.qiao712.bbs.domain.AddGroup;
import github.qiao712.bbs.domain.UpdateGroup;
import github.qiao712.bbs.domain.base.Result;
import github.qiao712.bbs.domain.dto.Statistic;
import github.qiao712.bbs.domain.entity.Advertisement;
import github.qiao712.bbs.service.SystemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    //首页广告设置------------------------------------------------------
    @PostMapping("/home-ads/images")
    @PreAuthorize("hasAuthority('admin:sys:ads:add')")
    public Result<Long> uploadAdvertisementImage(@RequestPart("image") MultipartFile imageFile){
        return Result.succeedNotNull(systemService.uploadAdvertisementImage(imageFile));
    }

    @PostMapping("/home-ads")
    @PreAuthorize("hasAuthority('admin:sys:ads:add')")
    public Result<Void> addAdvertisement(@Validated(AddGroup.class) @RequestBody Advertisement advertisement){
        return Result.build(systemService.addAdvertisement(advertisement));
    }

    @DeleteMapping("/home-ads/{advertisementId}")
    @PreAuthorize("hasAuthority('admin:sys:ads:remove')")
    public Result<Void> removeAdvertisement(@PathVariable Long advertisementId){
        return Result.build(systemService.removeAdvertisement(advertisementId));
    }

    @PutMapping("/home-ads")
    @PreAuthorize("hasAuthority('admin:sys:ads:update')")
    public Result<Void> updateAdvertisement(@Validated(UpdateGroup.class) @RequestBody Advertisement advertisement){
        return Result.build(systemService.updateAdvertisement(advertisement));
    }
}