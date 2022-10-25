package github.qiao712.bbs.domain.dto.message;

import lombok.Data;

/**
 * 用户问题或评论被回复消息
 */
@Data
public class ReplyMessageContent implements MessageContent {
    private Long questionId;
    private String questionTitle;
    private Long authorId;
    private String authorUsername;
    private Long commentId;
    private String comment;
}
