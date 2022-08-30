package github.qiao712.bbs.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import github.qiao712.bbs.domain.AddGroup;
import github.qiao712.bbs.domain.UpdateGroup;
import github.qiao712.bbs.domain.base.PageQuery;
import github.qiao712.bbs.domain.base.Result;
import github.qiao712.bbs.domain.entity.Forum;
import github.qiao712.bbs.service.ForumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/forums")
public class ForumController {
    @Autowired
    private ForumService forumService;

    @GetMapping("/{forumId}")
    public Result<Forum> getForum(@PathVariable("forumId") Long forumId){
        return Result.succeedNotNull(forumService.getForum(forumId));
    }

    @GetMapping
    public Result<IPage<Forum>> listForums(PageQuery pageQuery, Forum forum){
        return Result.succeed(forumService.listForums(pageQuery, forum));
    }

    @GetMapping("/all")
    public Result<List<Forum>> listAllForums(){
        return Result.succeed(forumService.listAllForums());
    }
}
