package sdu.addd.qasys.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import sdu.addd.qasys.common.Result;
import sdu.addd.qasys.entity.Tag;
import sdu.addd.qasys.service.TagService;
import sdu.addd.qasys.common.PageQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
public class CategoryController {
    @Autowired
    private TagService tagService;

    @GetMapping("/{tagId}")
    @PreAuthorize("hasAuthority('tag:get')")
    public Result<Tag> getCategory(@PathVariable("tagId") Long tagId){
        return Result.succeedNotNull(tagService.getById(tagId));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('tag:list')")
    public Result<IPage<Tag>> listCategories(PageQuery pageQuery, Tag tag){
        return Result.succeed(tagService.listTags(pageQuery, tag));
    }

    @GetMapping("/all")
    @PreAuthorize("hasAuthority('tag:list')")
    public Result<List<Tag>> listAllCategories(){
        return Result.succeed(tagService.listAllTags());
    }
}
