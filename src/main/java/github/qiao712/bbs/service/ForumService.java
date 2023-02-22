package github.qiao712.bbs.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import github.qiao712.bbs.domain.base.PageQuery;
import github.qiao712.bbs.domain.entity.Forum;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ForumService extends IService<Forum> {
    /**
     * 查询板块
     */
    Forum getForum(Long forumId);

    /**
     * 查询板块列表
     */
    IPage<Forum> listForums(PageQuery pageQuery, Forum condition);

    /**
     * 列出所有板块
     */
    List<Forum> listAllForums();

    /**
     * 添加板块
     */
    boolean addForum(Forum forum);

    /**
     * 更新板块
     */
    boolean updateForum(Forum forum);

    /**
     * 获取分类列表
     */
    List<String> listCategories();

    /**
     * 设置logo
     */
    boolean setForumLogo(Long forumId, Long fileId);

    /**
     * 获取贴子数量
     */
    Long getPostCount(Long forumId);

    /**
     * 增加贴子数量
     */
    void increasePostCount(Long forumId, Long delta);
}
