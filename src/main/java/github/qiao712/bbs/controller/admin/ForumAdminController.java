package github.qiao712.bbs.controller.admin;

import github.qiao712.bbs.domain.AddGroup;
import github.qiao712.bbs.domain.UpdateGroup;
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
@RequestMapping("/api/admin/forums")
public class ForumAdminController {
    @Autowired
    private ForumService forumService;

    @PostMapping
    @PreAuthorize("hasAuthority('admin:forum:add')")
    public Result<Void> addForum(@Validated(AddGroup.class) @RequestBody Forum forum){
        return Result.build(forumService.addForum(forum));
    }

    @DeleteMapping("/{forumId}")
    @PreAuthorize("hasAuthority('admin:forum:add')")
    public Result<Void> removeForum(@PathVariable("forumId") Long forumId){
        return Result.build(forumService.removeById(forumId));
    }

    @PutMapping
    @PreAuthorize("hasAuthority('admin:forum:update')")
    public Result<Void> updateForum(@Validated(UpdateGroup.class) @RequestBody Forum forum){
        return Result.build(forumService.updateForum(forum));
    }

    @GetMapping("/categories")
    @PreAuthorize("hasAuthority('admin:forum:category:list')")
    public Result<List<String>> listCategories(){
        return Result.succeed(forumService.listCategories());
    }

    @PutMapping("/{forumId}/logo")
    @PreAuthorize("hasAuthority('admin:forum:logo:update')")
    public Result<Void> setForumLogo(@PathVariable Long forumId, String logoUrl){
        return Result.build(forumService.setForumLogo(forumId, logoUrl));
    }
}
