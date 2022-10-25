package github.qiao712.bbs.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import github.qiao712.bbs.domain.base.PageQuery;
import github.qiao712.bbs.domain.dto.PostDto;
import github.qiao712.bbs.domain.entity.Question;
import org.springframework.web.multipart.MultipartFile;

public interface PostService extends IService<Question> {

    boolean addPost(Question question);

    /**
     * 上传贴子中插入的图片
     * @return 图片的url
     */
    String uploadImage(MultipartFile image);

    PostDto getPost(Long postId);

    IPage<PostDto> listPosts(PageQuery pageQuery, Long forumId, String authorUsername);

    IPage<PostDto> searchPosts(PageQuery pageQuery, String text, Long forumId, Long authorId);

    boolean removePost(Long postId);

    boolean isAuthor(Long postId, Long userId);
}
