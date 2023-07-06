package sdu.addd.qasys.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import sdu.addd.qasys.common.Result;
import sdu.addd.qasys.service.CategoryService;
import sdu.addd.qasys.common.PageQuery;
import sdu.addd.qasys.entity.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    @GetMapping("/{categoryId}")
    @PreAuthorize("hasAuthority('category:get')")
    public Result<Category> getCategory(@PathVariable("categoryId") Long categoryId){
        return Result.succeedNotNull(categoryService.getCategory(categoryId));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('category:list')")
    public Result<IPage<Category>> listCategories(PageQuery pageQuery, Category category){
        return Result.succeed(categoryService.listCategories(pageQuery, category));
    }

    @GetMapping("/all")
    @PreAuthorize("hasAuthority('category:list')")
    public Result<List<Category>> listAllCategories(){
        return Result.succeed(categoryService.listAllCategories());
    }
}
