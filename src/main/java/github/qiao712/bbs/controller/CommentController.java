package github.qiao712.bbs.controller;

import github.qiao712.bbs.domain.AddGroup;
import github.qiao712.bbs.domain.base.Result;
import github.qiao712.bbs.domain.entity.Comment;
import github.qiao712.bbs.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/comments")
public class CommentController {
    @Autowired
    private CommentService commentService;

    @PostMapping
    public Result<Void> addComment(@Validated(AddGroup.class) @RequestBody Comment comment){
        return Result.build(commentService.addComment(comment));
    }
}
