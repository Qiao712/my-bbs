package github.qiao712.bbs.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import github.qiao712.bbs.domain.AddGroup;
import github.qiao712.bbs.domain.base.PageQuery;
import github.qiao712.bbs.domain.base.Result;
import github.qiao712.bbs.domain.base.ResultCode;
import github.qiao712.bbs.domain.dto.AuthUser;
import github.qiao712.bbs.domain.entity.Answer;
import github.qiao712.bbs.exception.ServiceException;
import github.qiao712.bbs.service.AnswerService;
import github.qiao712.bbs.service.LikeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 答案 前端控制器
 * </p>
 *
 * @author qiao712
 * @since 2023-07-05
 */
@RestController
@RequestMapping("/api/answers")
public class AnswerController {
    @Autowired
    private AnswerService answerService;
    @Autowired
    private LikeService likeService;

    @PostMapping
    @PreAuthorize("isAuthenticated() and hasAuthority('answer:add')")
    public Result<Void> addAnswer(@Validated(AddGroup.class) @RequestBody Answer answer){
        return Result.build(answerService.addAnswer(answer));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('answer:list')")
    public Result<IPage<Answer>> listAnswers(@Validated PageQuery pageQuery, Long questionId){
        return Result.succeed(answerService.listAnswers(pageQuery, questionId));
    }

    @DeleteMapping("/{answerId}")
    @PreAuthorize("isAuthenticated() and hasAuthority('answer:remove:mine')")
    public Result<Void> removeMyComment(@PathVariable("answerId") Long answerId, @AuthenticationPrincipal AuthUser currentUser){
        if(!answerService.isAuthor(answerId, currentUser.getId())){
            throw new ServiceException(ResultCode.NO_PERMISSION, "无权删除答案");
        }
        return Result.build(answerService.removeAnswer(answerId));
    }

    //点赞------------------------------
    @GetMapping("/{answerId}/like")
    @PreAuthorize("isAuthenticated() and hasAuthority('answer:like')")
    public Result<Void> likeAnswer(@PathVariable("answerId") Long answerId){
        likeService.likeAnswer(answerId, true);
        return Result.succeed();
    }

    @GetMapping("/{answerId}/undo-like")
    @PreAuthorize("isAuthenticated() and hasAuthority('answer:like')")
    public Result<Void> undoLikeAnswer(@PathVariable("answerId") Long answerId){
        likeService.likeAnswer(answerId, false);
        return Result.succeed();
    }
}
