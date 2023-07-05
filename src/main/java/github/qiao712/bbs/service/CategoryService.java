package github.qiao712.bbs.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import github.qiao712.bbs.domain.base.PageQuery;
import github.qiao712.bbs.domain.entity.Category;

import java.util.List;

public interface CategoryService extends IService<Category> {
    Category getCategory(Long categoryId);

    IPage<Category> listCategories(PageQuery pageQuery, Category condition);

    List<Category> listAllCategories();

    boolean addCategory(Category category);

    boolean updateCategory(Category category);

    /**
     * 获取分类列表
     */
    List<String> listCategories();

    boolean setCategoryLogo(Long categoryId, String logoUrl);
}
