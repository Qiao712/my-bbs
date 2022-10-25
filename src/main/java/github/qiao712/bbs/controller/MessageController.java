package github.qiao712.bbs.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import github.qiao712.bbs.domain.base.PageQuery;
import github.qiao712.bbs.domain.base.Result;
import github.qiao712.bbs.domain.dto.MessageDto;
import github.qiao712.bbs.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/messages")
public class MessageController {
    @Autowired
    private MessageService messageService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public Result<IPage<MessageDto>> listSystemMessages(@Validated PageQuery pageQuery){
        return Result.succeed(messageService.listSystemMessages(pageQuery));
    }

    @GetMapping("/count")
    @PreAuthorize("isAuthenticated()")
    public Result<Long> getUnacknowledgedSystemMessageCount(){
        return Result.succeed(messageService.getUnacknowledgedSystemMessageCount());
    }
}
