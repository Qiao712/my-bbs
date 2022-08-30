package github.qiao712.bbs.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import github.qiao712.bbs.domain.base.PageQuery;
import github.qiao712.bbs.domain.base.Result;
import github.qiao712.bbs.domain.dto.CommentDetailDto;
import github.qiao712.bbs.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/comments")
public class CommentAdminController {
    @Autowired
    private CommentService commentService;

    /**
     * 查询评论列表
     * 用于后台管理评论列表
     */
    @GetMapping("/details")
    @PreAuthorize("hasAuthority('admin:comment:list')")
    public Result<IPage<CommentDetailDto>> listCommentDetails(@Validated PageQuery pageQuery, String authorUsername){
        return Result.succeed(commentService.listCommentsByAuthor(pageQuery, authorUsername));
    }

    @DeleteMapping("/{commentId}")
    @PreAuthorize("hasAuthority('admin:comment:remove')")
    public Result<Void> removeComment(@PathVariable("commentId") Long commentId){
        return Result.build(commentService.removeComment(commentId));
    }
}
