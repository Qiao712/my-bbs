package github.qiao712.bbs.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import github.qiao712.bbs.domain.base.PageQuery;
import github.qiao712.bbs.domain.entity.Forum;
import github.qiao712.bbs.exception.ServiceException;
import github.qiao712.bbs.mapper.ForumMapper;
import github.qiao712.bbs.service.ForumService;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ForumServiceImpl extends ServiceImpl<ForumMapper, Forum> implements ForumService {
    @Autowired
    private ForumMapper forumMapper;

    @Override
    public IPage<Forum> listForums(PageQuery pageQuery, Forum condition) {
        QueryWrapper<Forum> queryWrapper = new QueryWrapper<>();
        if(Strings.isNotBlank(condition.getName())){
            queryWrapper.like("name", '%' + condition.getName() + '%');
        }
        if(Strings.isNotBlank(condition.getCategory())){
            queryWrapper.eq("category", condition.getCategory());
        }
        return forumMapper.selectPage(pageQuery.getIPage(), queryWrapper);
    }

    @Override
    public boolean addForum(Forum forum) {
        if(getForumByName(forum.getName()) != null){
            throw new ServiceException("同名板块已存在");
        }

        return forumMapper.insert(forum) > 0;
    }

    @Override
    public boolean updateForum(Forum forum) {
        Forum forum2 = getForumByName(forum.getName());
        if(forum2 != null && !Objects.equals(forum.getId(), forum2.getId())){
            throw new ServiceException("同名板块已存在");
        }

        return forumMapper.updateById(forum) > 0;
    }

    @Override
    public List<String> listCategories() {
        QueryWrapper<Forum> queryWrapper = new QueryWrapper<>();
        queryWrapper.groupBy("category");
        queryWrapper.select("category");
        List<Forum> forums = forumMapper.selectList(queryWrapper);
        return forums.stream().map(Forum::getCategory).collect(Collectors.toList());
    }

    private Forum getForumByName(String forumName){
        Forum forumQuery = new Forum();
        forumQuery.setName(forumName);
        return forumMapper.selectOne(new QueryWrapper<>(forumQuery));
    }
}
