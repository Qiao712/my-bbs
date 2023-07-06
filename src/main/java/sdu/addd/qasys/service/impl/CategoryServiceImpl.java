package sdu.addd.qasys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import sdu.addd.qasys.common.PageQuery;
import sdu.addd.qasys.common.ResultCode;
import sdu.addd.qasys.entity.Category;
import sdu.addd.qasys.exception.ServiceException;
import sdu.addd.qasys.mapper.CategoryMapper;
import sdu.addd.qasys.service.CategoryService;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {
    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    public Category getCategory(Long categoryId) {
        return categoryMapper.selectById(categoryId);
    }

    @Override
    public IPage<Category> listCategories(PageQuery pageQuery, Category condition) {
        QueryWrapper<Category> queryWrapper = new QueryWrapper<>();
        if(Strings.isNotBlank(condition.getName())){
            queryWrapper.like("name", '%' + condition.getName() + '%');
        }
        if(Strings.isNotBlank(condition.getCategory())){
            queryWrapper.eq("category", condition.getCategory());
        }

        return categoryMapper.selectPage(pageQuery.getIPage(), queryWrapper);
    }

    @Override
    public List<Category> listAllCategories() {
        return categoryMapper.selectList(null);
    }

    @Override
    public boolean addCategory(Category category) {
        if(getForumByName(category.getName()) != null){
            throw new ServiceException(ResultCode.INVALID_PARAM, "同名板块已存在");
        }

        return categoryMapper.insert(category) > 0;
    }

    @Override
    public boolean updateCategory(Category category) {
        Category category2 = getForumByName(category.getName());
        if(category2 != null && !Objects.equals(category.getId(), category2.getId())){
            throw new ServiceException(ResultCode.INVALID_PARAM, "同名板块已存在");
        }

        return categoryMapper.updateById(category) > 0;
    }

    @Override
    public List<String> listCategories() {
        QueryWrapper<Category> queryWrapper = new QueryWrapper<>();
        queryWrapper.groupBy("category");
        queryWrapper.select("category");
        List<Category> categories = categoryMapper.selectList(queryWrapper);
        return categories.stream().map(Category::getCategory).collect(Collectors.toList());
    }

    @Override
    public boolean setCategoryLogo(Long categoryId, String logoUrl) {
        Category category = new Category();
        category.setId(categoryId);
        category.setLogoUrl(logoUrl);
        return categoryMapper.updateById(category) > 0;
    }

    private Category getForumByName(String forumName){
        Category categoryQuery = new Category();
        categoryQuery.setName(forumName);
        return categoryMapper.selectOne(new QueryWrapper<>(categoryQuery));
    }
}
