package qiao.qasys.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import qiao.qasys.common.PageQuery;
import qiao.qasys.dto.CommentDto;
import qiao.qasys.entity.Comment;

public interface CommentService extends IService<Comment> {
    /**
     * 添加评论
     */
    boolean addComment(Comment comment);

    /**
     * 查询回答的评论
     */
    IPage<CommentDto> listComments(PageQuery pageQuery, Long answerId);

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
