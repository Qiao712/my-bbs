package qiao.qasys.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import qiao.qasys.common.PageQuery;
import qiao.qasys.dto.AuthUser;
import qiao.qasys.dto.QuestionDto;
import qiao.qasys.common.AddGroup;
import qiao.qasys.common.Result;
import qiao.qasys.common.ResultCode;
import qiao.qasys.entity.Question;
import qiao.qasys.exception.ServiceException;
import qiao.qasys.service.LikeService;
import qiao.qasys.service.QuestionService;
import org.hibernate.validator.constraints.Length;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/{questionId}")
    @PreAuthorize("hasAuthority('question:get')")
    public Result<QuestionDto> getQuestion(@PathVariable("questionId") Long questionId){
        return Result.succeedNotNull(questionService.getQuestion(questionId));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('question:list')")
    public Result<IPage<QuestionDto>> listQuestions(@Validated PageQuery pageQuery, Long categoryId, Long authorId){
        return Result.succeed(questionService.listQuestion(pageQuery, categoryId, authorId));
    }

    @GetMapping("/search")
    @PreAuthorize("hasAuthority('question:search')")
    public Result<IPage<QuestionDto>> searchQuestions(@Validated PageQuery pageQuery,
                                                      @NotNull @NotBlank @Length(max = 30) String text,
                                                      Long authorId, Long categoryId){
        return Result.succeed(questionService.searchQuestion(pageQuery, text, categoryId, authorId));
    }

    @DeleteMapping("/{questionId}")
    @PreAuthorize("isAuthenticated() and hasAuthority('question:remove:mine')")
    public Result<Void> removeMyQuestion(@PathVariable("questionId") Long questionId, @AuthenticationPrincipal AuthUser currentUser){
        if(!questionService.isAuthor(questionId, currentUser.getId())){
            throw new ServiceException(ResultCode.NO_PERMISSION, "无权删除");
        }
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