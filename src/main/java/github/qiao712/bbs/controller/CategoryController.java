package github.qiao712.bbs.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import github.qiao712.bbs.domain.base.PageQuery;
import github.qiao712.bbs.domain.base.Result;
import github.qiao712.bbs.domain.entity.Category;
import github.qiao712.bbs.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/forums")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    @GetMapping("/{forumId}")
    @PreAuthorize("hasAuthority('forum:get')")
    public Result<Category> getCategory(@PathVariable("forumId") Long forumId){
        return Result.succeedNotNull(categoryService.getCategory(forumId));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('forum:list')")
    public Result<IPage<Category>> listCategories(PageQuery pageQuery, Category category){
        return Result.succeed(categoryService.listCategories(pageQuery, category));
    }

    @GetMapping("/all")
    @PreAuthorize("hasAuthority('forum:list')")
    public Result<List<Category>> listAllCategories(){
        return Result.succeed(categoryService.listAllCategories());
    }
}
