package github.qiao712.bbs.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import github.qiao712.bbs.domain.base.PageQuery;
import github.qiao712.bbs.domain.base.Result;
import github.qiao712.bbs.domain.dto.ConversationDto;
import github.qiao712.bbs.domain.dto.PrivateMessageDto;
import github.qiao712.bbs.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/chat")
public class ChatController {
    @Autowired
    private ChatService chatService;

    @GetMapping("/conversations")
    @PreAuthorize("isAuthenticated() and hasAuthority('message:conversation:list')")
    public Result<IPage<ConversationDto>> listConversations(@Validated PageQuery pageQuery){
        return Result.succeed(chatService.listConversations(pageQuery));
    }

    @GetMapping("/messages")
    @PreAuthorize("isAuthenticated() and hasAuthority('message:private:list')")
    public Result<List<PrivateMessageDto>> listPrivateMessages(@NotNull Long receiverId,
                                                               @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime after,
                                                               @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime before,
                                                               Integer limit){
        return Result.succeed(chatService.listPrivateMessages(receiverId, after, before, limit));
    }

    @GetMapping("/messages/count")
    @PreAuthorize("isAuthenticated()")
    public Result<Long> getUnacknowledgedPrivateMessageCount(){
        return Result.succeed(chatService.getUnacknowledgedPrivateMessageCount());
    }
}
