package github.qiao712.bbs.event;

import github.qiao712.bbs.domain.entity.Post;
import org.springframework.context.ApplicationEvent;

public class PostEvent extends ApplicationEvent {
    public enum PostEventType{
        CREATE,
        DELETE,
        UPDATE
    }

    private Post post;
    private Long postId;
    private PostEventType postEventType;

    private PostEvent(Object source) {
        super(source);
    }

    public static PostEvent buildCreatePostEvent(Post post, Object source){
        PostEvent postEvent = new PostEvent(source);
        postEvent.postEventType = PostEventType.CREATE;
        postEvent.post = post;
        return postEvent;
    }

    public static PostEvent buildDeletePostEvent(Long postId, Object source){
        PostEvent postEvent = new PostEvent(source);
        postEvent.postEventType = PostEventType.DELETE;
        postEvent.postId = postId;
        return postEvent;
    }

    public static PostEvent buildUpdatePostEvent(Post post, Object source){
        PostEvent postEvent = new PostEvent(source);
        postEvent.postEventType = PostEventType.UPDATE;
        postEvent.post = post;
        return postEvent;
    }

    public Post getPost() {
        return post;
    }

    public Long getPostId() {
        return postId;
    }

    public PostEventType getPostEventType() {
        return postEventType;
    }
}
