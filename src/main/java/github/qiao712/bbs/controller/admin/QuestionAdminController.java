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
@RequestMapping("/api/admin/posts")
public class QuestionAdminController {
    @Autowired
    private QuestionService questionService;

    @DeleteMapping("/{postId}")
    @PreAuthorize("hasAuthority('admin:post:remove')")
    public Result<Void> removePost(@PathVariable("postId") Long postId){
        return Result.build(questionService.removeQuestion(postId));
    }
}
