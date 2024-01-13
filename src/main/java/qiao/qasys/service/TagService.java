package qiao.qasys.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import qiao.qasys.common.PageQuery;
import qiao.qasys.entity.TagRelation;
import qiao.qasys.entity.Tag;

import java.util.List;

public interface TagService extends IService<Tag> {
    List<Tag> getTagsOfQuestion(Long questionId);

    Tag getOrCreateTagByName(String tagName);

    void saveTagRelations(List<TagRelation> relations);

    void removeTagRelationsByQuestionId(Long questionId);

    IPage<Tag> listTags(PageQuery pageQuery, Tag condition);

    List<Tag> listAllTags();

    boolean addTag(Tag tag);

    boolean updateTag(Tag tag);

    /**
     * 获取分类列表
     */
    List<String> listTags();

    boolean setTagLogo(Long tagId, String logoUrl);
}
