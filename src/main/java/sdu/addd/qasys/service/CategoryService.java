package sdu.addd.qasys.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import sdu.addd.qasys.common.PageQuery;
import sdu.addd.qasys.entity.Category;

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
