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
@RequestMapping("/api/admin/categories")
public class CategoryAdminController {
    @Autowired
    private CategoryService categoryService;

    @PostMapping
    @PreAuthorize("hasAuthority('admin:category:add')")
    public Result<Void> addCategory(@Validated(AddGroup.class) @RequestBody Category category){
        return Result.build(categoryService.addCategory(category));
    }

    @DeleteMapping("/{categoryId}")
    @PreAuthorize("hasAuthority('admin:category:add')")
    public Result<Void> removeCategory(@PathVariable("categoryId") Long categoryId){
        return Result.build(categoryService.removeById(categoryId));
    }

    @PutMapping
    @PreAuthorize("hasAuthority('admin:category:update')")
    public Result<Void> updateCategory(@Validated(UpdateGroup.class) @RequestBody Category category){
        return Result.build(categoryService.updateCategory(category));
    }

    @GetMapping("/categories")
    @PreAuthorize("hasAuthority('admin:category:list')")
    public Result<List<String>> listCategories(){
        return Result.succeed(categoryService.listCategories());
    }

    @PutMapping("/{categoryId}/logo")
    @PreAuthorize("hasAuthority('admin:category:logo:update')")
    public Result<Void> setCategoryLogo(@PathVariable Long categoryId, String logoUrl){
        return Result.build(categoryService.setCategoryLogo(categoryId, logoUrl));
    }
}
