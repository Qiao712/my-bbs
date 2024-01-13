package qiao.qasys.controller.admin;

import qiao.qasys.common.AddGroup;
import qiao.qasys.common.UpdateGroup;
import qiao.qasys.common.Result;
import qiao.qasys.entity.Tag;
import qiao.qasys.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/categories")
public class CategoryAdminController {
    @Autowired
    private TagService tagService;

    @PostMapping
    @PreAuthorize("hasAuthority('admin:category:add')")
    public Result<Void> addCategory(@Validated(AddGroup.class) @RequestBody Tag tag){
        return Result.build(tagService.addTag(tag));
    }

    @DeleteMapping("/{categoryId}")
    @PreAuthorize("hasAuthority('admin:category:add')")
    public Result<Void> removeCategory(@PathVariable("categoryId") Long categoryId){
        return Result.build(tagService.removeById(categoryId));
    }

    @PutMapping
    @PreAuthorize("hasAuthority('admin:category:update')")
    public Result<Void> updateCategory(@Validated(UpdateGroup.class) @RequestBody Tag tag){
        return Result.build(tagService.updateTag(tag));
    }

    @GetMapping("/categories")
    @PreAuthorize("hasAuthority('admin:category:list')")
    public Result<List<String>> listCategories(){
        return Result.succeed(tagService.listTags());
    }

    @PutMapping("/{categoryId}/logo")
    @PreAuthorize("hasAuthority('admin:category:logo:update')")
    public Result<Void> setCategoryLogo(@PathVariable Long categoryId, String logoUrl){
        return Result.build(tagService.setTagLogo(categoryId, logoUrl));
    }
}
