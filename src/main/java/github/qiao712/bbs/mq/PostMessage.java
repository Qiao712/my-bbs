package github.qiao712.bbs.mq;

import github.qiao712.bbs.domain.entity.Question;
import lombok.Data;

@Data
public class PostMessage {
    public enum PostMessageType {
        CREATE,
        DELETE,
        UPDATE
    }

    private Question question;
    private Long postId;
    private PostMessageType postMessageType;

    public static PostMessage buildPostAddMessage(Question question){
        PostMessage postMessage = new PostMessage();
        postMessage.postMessageType = PostMessageType.CREATE;
        postMessage.question = question;
        return postMessage;
    }

    public static PostMessage buildPostDeleteMessage(Long postId){
        PostMessage postMessage = new PostMessage();
        postMessage.postMessageType = PostMessageType.DELETE;
        postMessage.postId = postId;
        return postMessage;
    }

    public static PostMessage buildPostUpdateMessage(Question question){
        PostMessage postMessage = new PostMessage();
        postMessage.postMessageType = PostMessageType.UPDATE;
        postMessage.question = question;
        return postMessage;
    }
}
