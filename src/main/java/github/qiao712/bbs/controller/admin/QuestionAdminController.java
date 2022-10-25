package github.qiao712.bbs.controller.admin;

import github.qiao712.bbs.domain.base.Result;
import github.qiao712.bbs.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/questions")
public class QuestionAdminController {
    @Autowired
    private QuestionService questionService;

    @DeleteMapping("/{questionId}")
    @PreAuthorize("hasAuthority('admin:question:remove')")
    public Result<Void> removeQuestion(@PathVariable("questionId") Long questionId){
        return Result.build(questionService.removeQuestion(questionId));
    }
}
