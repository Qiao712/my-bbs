package github.qiao712.bbs.controller;

import github.qiao712.bbs.domain.base.Result;
import github.qiao712.bbs.domain.entity.Advertisement;
import github.qiao712.bbs.service.SystemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/sys")
public class SystemController {
    @Autowired
    private SystemService systemService;

    @GetMapping("/home-ads")
    public Result<List<Advertisement>> listAdvertisements(){
        return Result.succeed(systemService.listAdvertisements());
    }
}
