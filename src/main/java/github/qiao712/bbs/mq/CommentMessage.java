package github.qiao712.bbs.mq;

import github.qiao712.bbs.domain.entity.Comment;
import lombok.Data;

/**
 * 用于异步消息发送
 */
@Data
public class CommentMessage {
    public enum CommentMessageType {
        COMMENT_ADD,    //发送评论/回复消息
    }

    private CommentMessageType commentMessageType;
    private Comment comment;

    public static CommentMessage buildCommentAddMessage(Comment comment){
        CommentMessage commentMessage = new CommentMessage();
        commentMessage.commentMessageType = CommentMessageType.COMMENT_ADD;
        commentMessage.comment = comment;
        return commentMessage;
    }
}
