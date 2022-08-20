package github.qiao712.bbs.event;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import github.qiao712.bbs.domain.dto.message.ReplyMessageContent;
import github.qiao712.bbs.domain.entity.Comment;
import github.qiao712.bbs.domain.entity.Post;
import github.qiao712.bbs.mapper.CommentMapper;
import github.qiao712.bbs.mapper.PostMapper;
import github.qiao712.bbs.service.MessageService;
import github.qiao712.bbs.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class MessageEventListener implements ApplicationListener<MessageEvent> {
    @Autowired
    private MessageService messageService;
    @Autowired
    private UserService userService;
    @Autowired
    private PostMapper postMapper;
    @Autowired
    private CommentMapper commentMapper;

    @Async
    @Override
    public void onApplicationEvent(MessageEvent event) {
        switch (event.getMessageEventType()){
            case COMMENT_ADD:{
                sendReplyMessage(event.getComment());
                break;
            }

        }
    }

    /**
     * 发送评论回复消息
     */
    public void sendReplyMessage(Comment comment){
        ReplyMessageContent messageContent = new ReplyMessageContent();

        messageContent.setCommentId(comment.getId());
        messageContent.setComment(comment.getContent());
        messageContent.setAuthorId(comment.getAuthorId());
        messageContent.setAuthorUsername(userService.getUsername(comment.getAuthorId()));
        messageContent.setPostId(comment.getPostId());

        //设置贴子标题
        LambdaQueryWrapper<Post> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Post::getId, comment.getPostId());
        queryWrapper.select(Post::getTitle, Post::getAuthorId);
        Post post = postMapper.selectOne(queryWrapper);
        messageContent.setPostTitle(post.getTitle());

        //消息接收者
        Long receiverId = null;
        if(comment.getRepliedId() != null){
            //接收者为被回复评论的作者
            LambdaQueryWrapper<Comment> commentQueryWrapper = new LambdaQueryWrapper<>();
            commentQueryWrapper.select(Comment::getAuthorId);
            commentQueryWrapper.eq(Comment::getId, comment.getRepliedId());
            Comment repliedComment = commentMapper.selectOne(commentQueryWrapper);
            receiverId = repliedComment.getAuthorId();
        }else{
            //接收者为贴子作者
            receiverId = post.getAuthorId();
        }

        if(!Objects.equals(receiverId, comment.getAuthorId())){
            messageService.sendMessage(comment.getAuthorId(), receiverId, messageContent);
        }
    }
}
