package github.qiao712.bbs.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import github.qiao712.bbs.domain.base.PageQuery;
import github.qiao712.bbs.domain.dto.PostDto;
import github.qiao712.bbs.domain.entity.Post;
import org.springframework.web.multipart.MultipartFile;

public interface PostService extends IService<Post> {

    boolean addPost(Post post);

    PostDto getPost(Long postId);

    IPage<PostDto> listPosts(PageQuery pageQuery, Long forumId, String authorUsername);

    IPage<PostDto> searchPosts(PageQuery pageQuery, String text, Long forumId, Long authorId);

    boolean removePost(Long postId);

    boolean isAuthor(Long postId, Long userId);
}
