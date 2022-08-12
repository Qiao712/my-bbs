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

import java.util.List;

@RestController
@RequestMapping("/api/forums")
public class ForumController {
    @Autowired
    private ForumService forumService;

    @GetMapping("/{forumId}")
    public Result<Forum> getForum(@PathVariable("forumId") Long forumId){
        return Result.succeedNotNull(forumService.getById(forumId));
    }

    @GetMapping
    public Result<IPage<Forum>> listForums(PageQuery pageQuery, Forum forum){
        return Result.succeed(forumService.listForums(pageQuery, forum));
    }

    @PostMapping
    public Result<Void> addForum(@Validated(AddGroup.class) @RequestBody Forum forum){
        return Result.build(forumService.addForum(forum));
    }

    @DeleteMapping("/{forumId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Result<Void> removeForum(@PathVariable("forumId") Long forumId){
        return Result.build(forumService.removeById(forumId));
    }

    @PutMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Result<Void> updateForum(@Validated(UpdateGroup.class) @RequestBody Forum forum){
        return Result.build(forumService.updateForum(forum));
    }

    @GetMapping("/categories")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Result<List<String>> listCategories(){
        return Result.succeed(forumService.listCategories());
    }
}
