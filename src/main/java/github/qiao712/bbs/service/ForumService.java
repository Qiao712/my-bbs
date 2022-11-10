package github.qiao712.bbs.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import github.qiao712.bbs.domain.base.PageQuery;
import github.qiao712.bbs.domain.entity.Forum;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ForumService extends IService<Forum> {
    Forum getForum(Long forumId);

    IPage<Forum> listForums(PageQuery pageQuery, Forum condition);

    List<Forum> listAllForums();

    boolean addForum(Forum forum);

    boolean updateForum(Forum forum);

    /**
     * 获取分类列表
     */
    List<String> listCategories();

    boolean setForumLogo(Long forumId, Long fileId);
}
