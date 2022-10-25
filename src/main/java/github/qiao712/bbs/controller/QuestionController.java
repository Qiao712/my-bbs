package github.qiao712.bbs.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import github.qiao712.bbs.domain.AddGroup;
import github.qiao712.bbs.domain.base.PageQuery;
import github.qiao712.bbs.domain.base.Result;
import github.qiao712.bbs.domain.dto.AuthUser;
import github.qiao712.bbs.domain.dto.QuestionDto;
import github.qiao712.bbs.domain.entity.Question;
import github.qiao712.bbs.service.LikeService;
import github.qiao712.bbs.service.QuestionService;
import org.hibernate.validator.constraints.Length;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@RestController
@RequestMapping("/api/questions")
public class QuestionController {
    @Autowired
    private QuestionService questionService;
    @Autowired
    private LikeService likeService;

    @PostMapping
    @PreAuthorize("isAuthenticated() and hasAuthority('question:add')")
    public Result<Void> addQuestion(@Validated(AddGroup.class) @RequestBody Question question){
        return Result.build(questionService.addQuestion(question));
    }

    @PostMapping("/images")
    @PreAuthorize("isAuthenticated() and hasAuthority('question:image:upload')")
    public Result<String> uploadImage(@RequestPart("image") MultipartFile imageFile){
        return Result.succeed("图片上传成功", questionService.uploadImage(imageFile));
    }

    @GetMapping("/{questionId}")
    @PreAuthorize("hasAuthority('question:get')")
    public Result<QuestionDto> getQuestion(@PathVariable("questionId") Long questionId){
        return Result.succeedNotNull(questionService.getQuestion(questionId));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('question:list')")
    public Result<IPage<QuestionDto>> listQuestions(@Validated PageQuery pageQuery, Long forumId, String authorUsername){
        return Result.succeed(questionService.listQuestion(pageQuery, forumId, authorUsername));
    }

    @GetMapping("/search")
    @PreAuthorize("hasAuthority('question:search')")
    public Result<IPage<QuestionDto>> searchQuestions(@Validated PageQuery pageQuery,
                                                  @NotNull @NotBlank @Length(max = 30) String text,
                                                  Long authorId, Long forumId){
        return Result.succeed(questionService.searchQuestions(pageQuery, text, authorId, forumId));
    }

    @DeleteMapping("/{questionId}")
    @PreAuthorize("isAuthenticated() and questionService.isAuthor(questionId, currentUser.id) and hasAuthority('question:remove:mine')")
    public Result<Void> removeMyQuestion(@PathVariable("questionId") Long questionId, @AuthenticationPrincipal AuthUser currentUser){
        return Result.build(questionService.removeQuestion(questionId));
    }

    //-----------------------------------------
    @GetMapping("/{questionId}/like")
    @PreAuthorize("isAuthenticated() and hasAuthority('question:like')")
    public Result<Void> likeQuestion(@PathVariable("questionId") Long questionId){
        likeService.likeQuestion(questionId, true);
        return Result.succeed();
    }

    @GetMapping("/{questionId}/undo-like")
    @PreAuthorize("isAuthenticated() and hasAuthority('question:like')")
    public Result<Void> undoLikeQuestion(@PathVariable("questionId") Long questionId){
        likeService.likeQuestion(questionId, false);
        return Result.succeed();
    }
}
