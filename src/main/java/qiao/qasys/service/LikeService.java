package qiao.qasys.service;

import qiao.qasys.entity.QuestionLike;
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
     * 回答点赞/取消点赞
     * @param like true:点赞; false: 取消点赞
     */
    void likeAnswer(Long answerId, boolean like);

    /**
     * 检查用户对问题点赞
     */
    boolean hasLikedAnswer(Long answerId, Long userId);

    /**
     * 评论点赞/取消点赞
     * @param like true:点赞; false: 取消点赞
     */
    void likeComment(Long commentId, boolean like);

    /**
     * 检查用户对评论点赞
     */
    boolean hasLikedComment(Long commentId, Long userId);
}
