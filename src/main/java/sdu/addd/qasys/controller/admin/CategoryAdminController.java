package sdu.addd.qasys.controller.admin;

import sdu.addd.qasys.common.AddGroup;
import sdu.addd.qasys.common.UpdateGroup;
import sdu.addd.qasys.common.Result;
import sdu.addd.qasys.entity.Category;
import sdu.addd.qasys.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/forums")
public class CategoryAdminController {
    @Autowired
    private CategoryService categoryService;

    @PostMapping
    @PreAuthorize("hasAuthority('admin:forum:add')")
    public Result<Void> addCategory(@Validated(AddGroup.class) @RequestBody Category category){
        return Result.build(categoryService.addCategory(category));
    }

    @DeleteMapping("/{forumId}")
    @PreAuthorize("hasAuthority('admin:forum:add')")
    public Result<Void> removeCategory(@PathVariable("forumId") Long forumId){
        return Result.build(categoryService.removeById(forumId));
    }

    @PutMapping
    @PreAuthorize("hasAuthority('admin:forum:update')")
    public Result<Void> updateCategory(@Validated(UpdateGroup.class) @RequestBody Category category){
        return Result.build(categoryService.updateCategory(category));
    }

    @GetMapping("/categories")
    @PreAuthorize("hasAuthority('admin:forum:category:list')")
    public Result<List<String>> listCategories(){
        return Result.succeed(categoryService.listCategories());
    }

    @PutMapping("/{forumId}/logo")
    @PreAuthorize("hasAuthority('admin:forum:logo:update')")
    public Result<Void> setCategoryLogo(@PathVariable Long forumId, String logoUrl){
        return Result.build(categoryService.setCategoryLogo(forumId, logoUrl));
    }
}
