package github.qiao712.bbs.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import github.qiao712.bbs.domain.AddGroup;
import github.qiao712.bbs.domain.base.PageQuery;
import github.qiao712.bbs.domain.base.Result;
import github.qiao712.bbs.domain.dto.AuthUser;
import github.qiao712.bbs.domain.dto.PostDto;
import github.qiao712.bbs.domain.entity.Question;
import github.qiao712.bbs.domain.base.ResultCode;
import github.qiao712.bbs.exception.ServiceException;
import github.qiao712.bbs.service.LikeService;
import github.qiao712.bbs.service.QuestionService;
import org.hibernate.validator.constraints.Length;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@RestController
@RequestMapping("/api/posts")
public class QuestionController {
    @Autowired
    private QuestionService questionService;
    @Autowired
    private LikeService likeService;

    @PostMapping
    @PreAuthorize("isAuthenticated() and hasAuthority('post:add')")
    public Result<Void> addQuestion(@Validated(AddGroup.class) @RequestBody Question question){
        return Result.build(questionService.addQuestion(question));
    }

    @GetMapping("/{postId}")
    @PreAuthorize("hasAuthority('post:get')")
    public Result<PostDto> getQuestion(@PathVariable("postId") Long postId){
        return Result.succeedNotNull(questionService.getQuestion(postId));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('post:list')")
    public Result<IPage<PostDto>> listQuestions(@Validated PageQuery pageQuery, Long forumId, Long authorId){
        return Result.succeed(questionService.listQuestion(pageQuery, forumId, authorId));
    }

    @GetMapping("/search")
    @PreAuthorize("hasAuthority('post:search')")
    public Result<IPage<PostDto>> searchQuestions(@Validated PageQuery pageQuery,
                                                  @NotNull @NotBlank @Length(max = 30) String text,
                                                  Long authorId, Long forumId){
        return Result.succeed(questionService.searchQuestion(pageQuery, text, forumId, authorId));
    }

    @DeleteMapping("/{postId}")
    @PreAuthorize("isAuthenticated() and hasAuthority('post:remove:mine')")
    public Result<Void> removeMyQuestion(@PathVariable("postId") Long postId, @AuthenticationPrincipal AuthUser currentUser){
        if(!questionService.isAuthor(postId, currentUser.getId())){
            throw new ServiceException(ResultCode.NO_PERMISSION, "无权删除评论");
        }
        return Result.build(questionService.removeQuestion(postId));
    }

    //-----------------------------------------
    @GetMapping("/{postId}/like")
    @PreAuthorize("isAuthenticated() and hasAuthority('post:like')")
    public Result<Void> likeQuestion(@PathVariable("postId") Long postId){
        likeService.likeQuestion(postId, true);
        return Result.succeed();
    }

    @GetMapping("/{postId}/undo-like")
    @PreAuthorize("isAuthenticated() and hasAuthority('post:like')")
    public Result<Void> undoLikeQuestion(@PathVariable("postId") Long postId){
        likeService.likeQuestion(postId, false);
        return Result.succeed();
    }
}
