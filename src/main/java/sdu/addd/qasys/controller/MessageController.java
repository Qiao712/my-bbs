package sdu.addd.qasys.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import sdu.addd.qasys.common.Result;
import sdu.addd.qasys.dto.MessageDto;
import sdu.addd.qasys.service.MessageService;
import sdu.addd.qasys.common.PageQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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
