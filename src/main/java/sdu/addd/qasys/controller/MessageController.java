package sdu.addd.qasys.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import sdu.addd.qasys.common.PageQuery;
import sdu.addd.qasys.common.Result;
import sdu.addd.qasys.common.ResultCode;
import sdu.addd.qasys.dto.ConversationDto;
import sdu.addd.qasys.entity.Message;
import sdu.addd.qasys.exception.ServiceException;
import sdu.addd.qasys.service.MessageService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/chat")
public class MessageController {
    @Autowired
    private MessageService messageService;

    @GetMapping("/conversations")
    @PreAuthorize("isAuthenticated() and hasAuthority('chat:conversation:list')")
    public Result<IPage<ConversationDto>> listConversations(@Validated PageQuery pageQuery){
        return Result.succeed(messageService.listConversations(pageQuery));
    }

    @PutMapping("/messages/{receiverId}")
    @PreAuthorize("isAuthenticated() and hasAuthority('chat:conversation:send')")
    public Result<Void> sendMessage(@PathVariable Long receiverId, @RequestBody String[] content){
        if(content.length != 1) throw new ServiceException(ResultCode.INVALID_PARAM);
        messageService.sendMessage(receiverId, content[0]);
        return Result.succeed();
    }

    @GetMapping("/messages")
    @PreAuthorize("isAuthenticated() and hasAuthority('chat:private:list')")
    public Result<List<Message>> listMessages(Long userId,  //对话另一方用户
                                              @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime after,
                                              @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime before,
                                              Integer limit){
        return Result.succeed(messageService.listMessages(userId, after, before, limit));
    }

    @PutMapping("/messages/acknowledge")
    public Result<Void> acknowledge(Long senderId){
        messageService.acknowledge(senderId);
        return Result.succeed();
    }

    @GetMapping("/messages/unread-num")
    @PreAuthorize("isAuthenticated()")
    public Result<Long> getTotalUnreadNumber(){
        return Result.succeed(messageService.getUnreadNumber());
    }
}
