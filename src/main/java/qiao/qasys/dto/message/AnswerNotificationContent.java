package qiao.qasys.dto.message;

import lombok.Getter;
import lombok.Setter;

/**
 * 用户贴子或评论被回复消息
 */
@Getter
@Setter
public class AnswerNotificationContent extends NotificationContent {
    private Long questionId;
    private String questionTitle;
    private Long authorId;
    private String answer;
    private String authorUsername;
    private Long answerId;

    public AnswerNotificationContent() {
        super("answer");
    }


}
