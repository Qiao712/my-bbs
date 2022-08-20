package github.qiao712.bbs.domain.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户间私信会话信息
 */
@Data
public class ConversationDto {
    private String latestMessage;           //最后一条消息
    private Long unacknowledgedCount;       //未读消息数量
    private LocalDateTime createTime;

    //对方用户信息
    private Long userId;
    private String username;
    private String avatarUrl;
}
