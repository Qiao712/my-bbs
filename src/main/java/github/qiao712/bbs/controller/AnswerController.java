package github.qiao712.bbs.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import github.qiao712.bbs.domain.AddGroup;
import github.qiao712.bbs.domain.base.PageQuery;
import github.qiao712.bbs.domain.base.Result;
import github.qiao712.bbs.domain.dto.AnswerDetailDto;
import github.qiao712.bbs.domain.dto.AnswerDto;
import github.qiao712.bbs.domain.dto.AuthUser;
import github.qiao712.bbs.domain.entity.Answer;
import github.qiao712.bbs.exception.ServiceException;
import github.qiao712.bbs.service.AnswerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/answers")
public class AnswerController {
    @Autowired
    private AnswerService answerService;

    @PostMapping
    @PreAuthorize("isAuthenticated() and hasAuthority('answer:add')")
    public Result<Void> addAnswer(@Validated(AddGroup.class) @RequestBody Answer answer){
        return Result.build(answerService.addAnswer(answer));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('answer:list')")
    public Result<IPage<AnswerDto>> listAnswers(@Validated PageQuery pageQuery, Long questionId){
        return Result.succeed(answerService.listAnswers(pageQuery, questionId));
    }

    @GetMapping("/my")
    @PreAuthorize("isAuthenticated() and hasAuthority('answer:list:mine')")
    public Result<IPage<AnswerDetailDto>> listMyAnswers(@Validated PageQuery pageQuery, @AuthenticationPrincipal AuthUser authUser){
        return Result.succeed(answerService.listAnswersByAuthor(pageQuery, authUser.getUsername()));
    }

    @DeleteMapping("/{answerId}")
    @PreAuthorize("isAuthenticated() and hasAuthority('answer:remove:mine')")
    public Result<Void> removeMyAnswer(@PathVariable("answerId") Long answerId, @AuthenticationPrincipal AuthUser currentUser){
        if(!answerService.isAuthor(answerId, currentUser.getId())){
            throw new ServiceException("无权删除回答");
        }
        return Result.build(answerService.removeAnswer(answerId));
    }
}
