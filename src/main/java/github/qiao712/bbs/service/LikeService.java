package github.qiao712.bbs.service;

import github.qiao712.bbs.domain.entity.QuestionLike;
import com.baomidou.mybatisplus.extension.service.IService;

public interface LikeService extends IService<QuestionLike> {
    /**
     * 问题点赞/取消点赞
     * @param like true:点赞; false: 取消点赞
     */
    void likeQuestion(Long questionId, boolean like);

    /**
     * 检查用户对问题点赞
     */
    boolean hasLikedQuestion(Long questionId, Long userId);

    /**
     * 从缓存中获取点赞数量
     * @return 无缓存的值，若返回null，说明近期未被点赞，数据库中的值即为最新的
     */
    Long getQuestionLikeCountFromCache(Long questionId);

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
     * 同步点问题赞数据至数据库
     */
    void syncQuestionLikeCount();

    /**
     * 同步点评论点数据至数据库
     */
    void syncCommentLikeCount();
}
