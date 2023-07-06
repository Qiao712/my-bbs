package sdu.addd.qasys.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import sdu.addd.qasys.common.PageQuery;
import sdu.addd.qasys.entity.Answer;

/**
 * <p>
 * 答案 服务类
 * </p>
 *
 * @author qiao712
 * @since 2023-07-05
 */
public interface AnswerService extends IService<Answer> {
    boolean addAnswer(Answer answer);

    IPage<Answer> listAnswers(PageQuery pageQuery, Long questionId);

    /**
     * 根据作者用户名获取评论
     * @return CommentDetailDto 附加贴子标题，板块，被回复者名
     */
//    IPage<CommentDetailDto> listCommentsByAuthor(PageQuery pageQuery, Long authorId);

    /**
     * 删除评论
     * 若为一级评论，则同时将其引用的附件标记为可删除的
     */
    boolean removeAnswer(Long answerId);

    boolean isAuthor(Long answerId, Long userId);
}
