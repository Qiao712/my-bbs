package github.qiao712.bbs.mq;

import github.qiao712.bbs.domain.entity.Post;
import lombok.Data;

@Data
public class PostMessage {
    public enum PostMessageType {
        CREATE,
        DELETE,
        UPDATE
    }

    private Post post;
    private Long postId;
    private PostMessageType postMessageType;

    public static PostMessage buildPostAddMessage(Post post){
        PostMessage postMessage = new PostMessage();
        postMessage.postMessageType = PostMessageType.CREATE;
        postMessage.post = post;
        return postMessage;
    }

    public static PostMessage buildPostDeleteMessage(Long postId){
        PostMessage postMessage = new PostMessage();
        postMessage.postMessageType = PostMessageType.DELETE;
        postMessage.postId = postId;
        return postMessage;
    }

    public static PostMessage buildPostUpdateMessage(Post post){
        PostMessage postMessage = new PostMessage();
        postMessage.postMessageType = PostMessageType.UPDATE;
        postMessage.post = post;
        return postMessage;
    }
}
