package github.qiao712.bbs.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import github.qiao712.bbs.domain.AddGroup;
import github.qiao712.bbs.domain.base.PageQuery;
import github.qiao712.bbs.domain.base.Result;
import github.qiao712.bbs.domain.dto.AuthUser;
import github.qiao712.bbs.domain.dto.CommentDetailDto;
import github.qiao712.bbs.domain.dto.CommentDto;
import github.qiao712.bbs.domain.entity.Comment;
import github.qiao712.bbs.exception.ServiceException;
import github.qiao712.bbs.service.CommentService;
import github.qiao712.bbs.service.LikeService;
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

    @GetMapping("/my")
    @PreAuthorize("isAuthenticated() and hasAuthority('comment:list:mine')")
    public Result<IPage<CommentDetailDto>> listMyComments(@Validated PageQuery pageQuery, @AuthenticationPrincipal AuthUser authUser){
        return Result.succeed(commentService.listCommentsByAuthor(pageQuery, authUser.getUsername()));
    }

    @DeleteMapping("/{commentId}")
    @PreAuthorize("isAuthenticated() and hasAuthority('comment:remove:mine')")
    public Result<Void> removeMyComment(@PathVariable("commentId") Long commentId, @AuthenticationPrincipal AuthUser currentUser){
        if(!commentService.isAuthor(commentId, currentUser.getId())){
            throw new ServiceException("无权删除评论");
        }
        return Result.build(commentService.removeComment(commentId));
    }
}
