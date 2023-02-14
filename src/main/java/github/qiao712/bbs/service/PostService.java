package github.qiao712.bbs.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import github.qiao712.bbs.domain.base.PageQuery;
import github.qiao712.bbs.domain.dto.PostDto;
import github.qiao712.bbs.domain.entity.Post;

import java.util.Collection;
import java.util.List;

public interface PostService extends IService<Post> {
    /**
     * 发布贴子
     */
    boolean addPost(Post post);

    /**
     * 获取贴子内容
     */
    PostDto getPost(Long postId);

    /**
     * 查询贴子列表
     */
    IPage<PostDto> listPosts(PageQuery pageQuery, Long forumId, Long authorId);

    /**
     * 批量查询贴子
     */
    List<PostDto> listPosts(Collection<Long> postIds);

    /**
     * 搜索贴子
     */
    IPage<PostDto> searchPosts(PageQuery pageQuery, String text, Long forumId, Long authorId);

    /**
     * 删除贴子
     */
    boolean removePost(Long postId);

    /**
     * 判断是否位贴子作者
     */
    boolean isAuthor(Long postId, Long userId);
}
