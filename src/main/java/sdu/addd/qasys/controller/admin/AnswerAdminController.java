package sdu.addd.qasys.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import sdu.addd.qasys.common.PageQuery;
import sdu.addd.qasys.common.Result;
import sdu.addd.qasys.entity.Answer;
import sdu.addd.qasys.service.AnswerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/answers")
public class AnswerAdminController {
    @Autowired
    private AnswerService answerService;

    @GetMapping("/details")
    @PreAuthorize("hasAuthority('admin:answer:list')")
    public Result<IPage<Answer>> listAnswerDetails(@Validated PageQuery pageQuery, Long authorId){
        return null;
    }

    @DeleteMapping("/{answerId}")
    @PreAuthorize("hasAuthority('admin:answer:remove')")
    public Result<Void> removeAnswer(@PathVariable("commentId") Long answer){
        return null;
    }
}
