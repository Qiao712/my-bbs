package github.qiao712.bbs.service;

import github.qiao712.bbs.domain.entity.PostLike;
import com.baomidou.mybatisplus.extension.service.IService;

public interface LikeService extends IService<PostLike> {
    /**
     * 贴子点赞/取消点赞
     * @param like true:点赞; false: 取消点赞
     */
    void likePost(Long postId, boolean like);

    /**
     * 检查用户对贴子点赞
     */
    boolean hasLikedPost(Long postId, Long userId);

    /**
     * 从缓存中获取点赞数量
     * @return 无缓存的值，若返回null，说明近期未被点赞，数据库中的值即为最新的
     */
    Long getPostLikeCountFromCache(Long postId);

    /**
     * 评论点赞/取消点赞
     * @param like true:点赞; false: 取消点赞
     */
    void likeComment(Long commentId, boolean like);

    /**
     * 检查用户对评论点赞
     */
    boolean hasLikedComment(Long commentId, Long userId);

    /**
     * 从缓存中获取点赞数量
     * @return 无缓存的值，若返回null，说明近期未被点赞，数据库中的值即为最新的
     */
    Long getCommentLikeCountFromCache(Long commentId);

    /**
     * 同步点贴子赞数据至数据库
     */
    void syncPostLikeCount();

    /**
     * 同步点评论点数据至数据库
     */
    void syncCommentLikeCount();
}
