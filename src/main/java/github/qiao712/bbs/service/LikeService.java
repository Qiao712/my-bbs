package github.qiao712.bbs.service;

import github.qiao712.bbs.domain.entity.PostLike;
import com.baomidou.mybatisplus.extension.service.IService;

public interface LikeService extends IService<PostLike> {
    /**
     * 贴子点赞
     */
    boolean likePost(Long postId);

    /**
     * 取消点赞
     */
    boolean undoLikePost(Long postId);

    /**
     * 检查用户对贴子点赞
     */
    boolean hasLikedPost(Long postId, Long userId);
}
