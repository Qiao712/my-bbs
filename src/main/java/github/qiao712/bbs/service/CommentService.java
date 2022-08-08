package github.qiao712.bbs.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import github.qiao712.bbs.domain.base.PageQuery;
import github.qiao712.bbs.domain.dto.CommentDto;
import github.qiao712.bbs.domain.entity.Comment;

public interface CommentService extends IService<Comment> {
    boolean addComment(Comment comment);

    /**
     * 查询贴子的评论
     * 若parentCommentId != null，则查询该贴子下的一级评论
     * 若parentCommentId == null，则查询该评论下的二级评论
     */
    IPage<CommentDto> listComments(PageQuery pageQuery, Long postId, Long parentCommentId);


}
