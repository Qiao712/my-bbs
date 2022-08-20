package github.qiao712.bbs.event;

import github.qiao712.bbs.domain.entity.Comment;
import org.springframework.context.ApplicationEvent;

/**
 * 用于异步消息发送
 */
public class MessageEvent extends ApplicationEvent {
    public enum MessageEventType{
        COMMENT_ADD,    //发送评论/回复消息
    }

    private MessageEventType messageEventType;
    private Comment comment;

    private MessageEvent(Object source) {
        super(source);
    }

    public static MessageEvent buildCommentAddEvent(Comment comment, Object source){
        MessageEvent messageEvent = new MessageEvent(source);
        messageEvent.messageEventType = MessageEventType.COMMENT_ADD;
        messageEvent.comment = comment;
        return messageEvent;
    }

    public MessageEventType getMessageEventType() {
        return messageEventType;
    }

    public Comment getComment() {
        return comment;
    }
}
