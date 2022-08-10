package github.qiao712.bbs.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import github.qiao712.bbs.domain.AddGroup;
import github.qiao712.bbs.domain.base.PageQuery;
import github.qiao712.bbs.domain.base.Result;
import github.qiao712.bbs.domain.dto.AuthUser;
import github.qiao712.bbs.domain.dto.CommentDto;
import github.qiao712.bbs.domain.entity.Comment;
import github.qiao712.bbs.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/comments")
public class CommentController {
    @Autowired
    private CommentService commentService;

    @PostMapping
    public Result<Void> addComment(@Validated(AddGroup.class) @RequestBody Comment comment){
        return Result.build(commentService.addComment(comment));
    }

    @GetMapping
    public Result<IPage<CommentDto>> getComments(PageQuery pageQuery, Long postId, Long parentCommentId){
        return Result.succeed(commentService.listComments(pageQuery, postId, parentCommentId));
    }

    @DeleteMapping("/{commentId}")
    public Result<Void> removeComment(@PathVariable("commentId") Long commentId, @AuthenticationPrincipal AuthUser currentUser){
        if(currentUser.getRole().equals("ROLE_ADMIN") || commentService.isAuthor(commentId, currentUser.getId())){
            return Result.build(commentService.removeComment(commentId));
        }else{
            throw new AccessDeniedException("无权删除该评论");
        }
    }
}
