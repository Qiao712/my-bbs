package github.qiao712.bbs.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import github.qiao712.bbs.domain.base.PageQuery;
import github.qiao712.bbs.domain.dto.CommentDetailDto;
import github.qiao712.bbs.domain.dto.CommentDto;
import github.qiao712.bbs.domain.entity.Comment;

public interface CommentService extends IService<Comment> {
    /**
     * 添加评论
     */
    boolean addComment(Comment comment);

    /**
     * 查询问题的评论
     * 若parentCommentId != null，则查询该问题下的一级评论
     * 若parentCommentId == null，则查询该评论下的二级评论
     */
    IPage<CommentDto> listComments(PageQuery pageQuery, Long answerId);

    /**
     * 根据作者用户名获取评论
     * @return CommentDetailDto 附加问题标题，板块，被回复者名
     */
    IPage<CommentDetailDto> listCommentsByAuthor(PageQuery pageQuery, String authorUsername);

    /**
     * 删除评论
     * 若为一级评论，则同时将其引用的附件标记为可删除的
     */
    boolean removeComment(Long commentId);

    /**
     * 判断用户是否为评论的发布者
     */
    boolean isAuthor(Long commentId, Long userId);
}
