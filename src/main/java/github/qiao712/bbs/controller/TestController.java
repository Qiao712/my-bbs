package github.qiao712.bbs.controller;

import github.qiao712.bbs.domain.base.Result;
import github.qiao712.bbs.service.LikeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class TestController {
    @Autowired
    private LikeService likeService;

    @GetMapping("/sync-like-count")
    public Result<Void> syncLikeCount(){
        likeService.syncPostLikeCount();
        return Result.succeed();
    }

    public int test(){
        System.out.println("----Quartz...----");
        return 1;
    }
}
