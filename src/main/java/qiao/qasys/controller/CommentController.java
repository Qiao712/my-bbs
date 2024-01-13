package qiao.qasys.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import qiao.qasys.common.PageQuery;
import qiao.qasys.dto.AuthUser;
import qiao.qasys.dto.CommentDto;
import qiao.qasys.entity.Comment;
import qiao.qasys.common.AddGroup;
import qiao.qasys.common.Result;
import qiao.qasys.common.ResultCode;
import qiao.qasys.exception.ServiceException;
import qiao.qasys.service.LikeService;
import qiao.qasys.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/comments")
public class CommentController {
    @Autowired
    private CommentService commentService;
    @Autowired
    private LikeService likeService;

    @PostMapping
    @PreAuthorize("isAuthenticated() and hasAuthority('comment:add')")
    public Result<Void> addComment(@Validated(AddGroup.class) @RequestBody Comment comment){
        return Result.build(commentService.addComment(comment));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('comment:list')")
    public Result<IPage<CommentDto>> listComments(@Validated PageQuery pageQuery, Long answerId){
        return Result.succeed(commentService.listComments(pageQuery, answerId));
    }

    @DeleteMapping("/{commentId}")
    @PreAuthorize("isAuthenticated() and hasAuthority('comment:remove:mine')")
    public Result<Void> removeMyComment(@PathVariable("commentId") Long commentId, @AuthenticationPrincipal AuthUser currentUser){
        if(!commentService.isAuthor(commentId, currentUser.getId())){
            throw new ServiceException(ResultCode.NO_PERMISSION, "无权删除评论");
        }
        return Result.build(commentService.removeComment(commentId));
    }

    //点赞------------------------------
    @GetMapping("/{commentId}/like")
    @PreAuthorize("isAuthenticated() and hasAuthority('comment:like')")
    public Result<Void> likeComment(@PathVariable("commentId") Long commentId){
        likeService.likeComment(commentId, true);
        return Result.succeed();
    }

    @GetMapping("/{commentId}/undo-like")
    @PreAuthorize("isAuthenticated() and hasAuthority('comment:like')")
    public Result<Void> undoLikeComment(@PathVariable("commentId") Long commentId){
        likeService.likeComment(commentId, false);
        return Result.succeed();
    }
}