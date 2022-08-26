package github.qiao712.bbs.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import github.qiao712.bbs.domain.AddGroup;
import github.qiao712.bbs.domain.base.PageQuery;
import github.qiao712.bbs.domain.base.Result;
import github.qiao712.bbs.domain.dto.AuthUser;
import github.qiao712.bbs.domain.dto.CommentDetailDto;
import github.qiao712.bbs.domain.dto.CommentDto;
import github.qiao712.bbs.domain.entity.Comment;
import github.qiao712.bbs.service.CommentService;
import github.qiao712.bbs.service.LikeService;
import github.qiao712.bbs.service.StatisticsService;
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
    @Autowired
    private LikeService likeService;

    @PostMapping
    public Result<Void> addComment(@Validated(AddGroup.class) @RequestBody Comment comment){
        return Result.build(commentService.addComment(comment));
    }

    @GetMapping
    public Result<IPage<CommentDto>> listComments(@Validated PageQuery pageQuery, Long postId, Long parentCommentId){
        return Result.succeed(commentService.listComments(pageQuery, postId, parentCommentId));
    }

    /**
     * 更加详细的评论对象
     * 用于后台管理评论列表，用户个人的评论列表
     */
    @GetMapping("/details")
    public Result<IPage<CommentDetailDto>> listCommentsByAuthor(@Validated PageQuery pageQuery,
                                                                @AuthenticationPrincipal AuthUser currentUser,
                                                                String authorUsername){
        if(! currentUser.getRole().equals("ROLE_ADMIN") && authorUsername == null){
            //禁止普通用户查询全部评论(不指定作者)
            throw new AccessDeniedException("无权查看全部评论");
        }

        return Result.succeed(commentService.listCommentsByAuthor(pageQuery, authorUsername));
    }

    @DeleteMapping("/{commentId}")
    public Result<Void> removeComment(@PathVariable("commentId") Long commentId, @AuthenticationPrincipal AuthUser currentUser){
        if(currentUser.getRole().equals("ROLE_ADMIN") || commentService.isAuthor(commentId, currentUser.getId())){
            return Result.build(commentService.removeComment(commentId));
        }else{
            throw new AccessDeniedException("无权删除该评论");
        }
    }

    //点赞------------------------------
    @GetMapping("/{commentId}/like")
    public Result<Void> likeComment(@PathVariable("commentId") Long commentId){
        return Result.build(likeService.likeComment(commentId));
    }

    @GetMapping("/{commentId}/undo-like")
    public Result<Void> undoLikeComment(@PathVariable("commentId") Long commentId){
        return Result.build(likeService.undoLikeComment(commentId));
    }
}
