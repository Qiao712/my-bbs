package github.qiao712.bbs.domain.dto.message;

import lombok.Data;

/**
 * 用户贴子或评论被回复消息
 */
@Data
public class AnswerMessageContent implements MessageContent {
    private Long questionId;
    private String questionTitle;
    private Long authorId;
    private String authorUsername;
    private Long answerId;
}
