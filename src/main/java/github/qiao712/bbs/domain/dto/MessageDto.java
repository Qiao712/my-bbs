package github.qiao712.bbs.domain.dto;

import github.qiao712.bbs.domain.dto.message.MessageContent;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MessageDto {
    private Long id;
    private Long senderId;
    private Long receiverId;
    private String type;
    private Boolean isAcknowledged;
    private MessageContent content;
    private LocalDateTime createTime;
}
