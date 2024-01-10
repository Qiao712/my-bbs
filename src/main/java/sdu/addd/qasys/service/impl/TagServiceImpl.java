package sdu.addd.qasys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.transaction.annotation.Transactional;
import sdu.addd.qasys.common.PageQuery;
import sdu.addd.qasys.common.ResultCode;
import sdu.addd.qasys.entity.Tag;
import sdu.addd.qasys.entity.TagRelation;
import sdu.addd.qasys.exception.ServiceException;
import sdu.addd.qasys.mapper.TagMapper;
import sdu.addd.qasys.mapper.TagRelationMapper;
import sdu.addd.qasys.service.TagService;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag> implements TagService {
    @Autowired
    private TagRelationMapper tagRelationMapper;

    @Override
    public List<Tag> getTagsOfQuestion(Long questionId) {
        return baseMapper.selectTagByQuestion(questionId);
    }

    @Override
    public Tag getOrCreateTagByName(String tagName) {
        LambdaQueryWrapper<Tag> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Tag::getName, tagName);
        Tag tag = getOne(queryWrapper);

        if(tag == null){
            //创建新标签
            tag = new Tag();
            tag.setName(tagName);
            save(tag);
        }

        return tag;
    }

    @Override
    @Transactional
    public void saveTagRelations(List<TagRelation> relations) {
        for (TagRelation relation : relations) {
            tagRelationMapper.insert(relation);
        }
    }

    @Override
    public void removeTagRelationsByQuestionId(Long questionId) {
        LambdaQueryWrapper<TagRelation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TagRelation::getQuestionId, questionId);
        tagRelationMapper.delete(queryWrapper);
    }

    @Override
    public IPage<Tag> listTags(PageQuery pageQuery, Tag condition) {
        QueryWrapper<Tag> queryWrapper = new QueryWrapper<>();
        if(Strings.isNotBlank(condition.getName())){
            queryWrapper.like("name", '%' + condition.getName() + '%');
        }
        if(Strings.isNotBlank(condition.getCategory())){
            queryWrapper.eq("category", condition.getCategory());
        }

        return baseMapper.selectPage(pageQuery.getIPage(), queryWrapper);
    }

    @Override
    public List<Tag> listAllTags() {
        return baseMapper.selectList(null);
    }

    @Override
    public boolean addTag(Tag tag) {
        if(getForumByName(tag.getName()) != null){
            throw new ServiceException(ResultCode.INVALID_PARAM, "同名板块已存在");
        }

        return baseMapper.insert(tag) > 0;
    }

    @Override
    public boolean updateTag(Tag tag) {
        Tag tag2 = getForumByName(tag.getName());
        if(tag2 != null && !Objects.equals(tag.getId(), tag2.getId())){
            throw new ServiceException(ResultCode.INVALID_PARAM, "同名板块已存在");
        }

        return baseMapper.updateById(tag) > 0;
    }

    @Override
    public List<String> listTags() {
        QueryWrapper<Tag> queryWrapper = new QueryWrapper<>();
        queryWrapper.groupBy("category");
        queryWrapper.select("category");
        List<Tag> categories = baseMapper.selectList(queryWrapper);
        return categories.stream().map(Tag::getCategory).collect(Collectors.toList());
    }

    @Override
    public boolean setTagLogo(Long tagId, String logoUrl) {
        Tag tag = new Tag();
        tag.setId(tagId);
        tag.setLogoUrl(logoUrl);
        return baseMapper.updateById(tag) > 0;
    }

    private Tag getForumByName(String forumName){
        Tag tagQuery = new Tag();
        tagQuery.setName(forumName);
        return baseMapper.selectOne(new QueryWrapper<>(tagQuery));
    }
}
