package github.qiao712.bbs.domain.dto.message;

import lombok.Data;

/**
 * 用户贴子或评论被回复消息
 */
@Data
public class ReplyMessageContent implements MessageContent {
    private Long postId;
    private String postTitle;
    private Long replierId;
    private String replierUsername;
    private Long commentId;
    private String comment;
}
