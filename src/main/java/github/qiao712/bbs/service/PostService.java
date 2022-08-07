package github.qiao712.bbs.service;

import com.baomidou.mybatisplus.extension.service.IService;
import github.qiao712.bbs.domain.entity.Post;
import org.springframework.web.multipart.MultipartFile;

public interface PostService extends IService<Post> {

    boolean addPost(Post post);

    /**
     * 上传图片
     * @return 图片的url
     */
    String uploadPicture(MultipartFile picture);
}
