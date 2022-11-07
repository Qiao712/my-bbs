package github.qiao712.bbs.mq.comment;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import github.qiao712.bbs.config.MQConfig;
import github.qiao712.bbs.domain.dto.message.ReplyMessageContent;
import github.qiao712.bbs.domain.entity.Comment;
import github.qiao712.bbs.domain.entity.Post;
import github.qiao712.bbs.mapper.CommentMapper;
import github.qiao712.bbs.mapper.PostMapper;
import github.qiao712.bbs.service.MessageService;
import github.qiao712.bbs.service.UserService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class CommentMessageListener {
    @Autowired
    private MessageService messageService;
    @Autowired
    private UserService userService;
    @Autowired
    private PostMapper postMapper;
    @Autowired
    private CommentMapper commentMapper;

    @KafkaListener(topics = {MQConfig.COMMENT_TOPIC}, groupId = "comment")
    public void onMessage(ConsumerRecord<String, String> consumerRecord){
        CommentMessage commentMessage = JSON.parseObject(consumerRecord.value(), CommentMessage.class);
        processMessage(commentMessage);
    }

    public void processMessage(CommentMessage commentMessage){
        switch (commentMessage.getCommentMessageType()){
            case COMMENT_ADD:
                sendCommentNoticeMessage(commentMessage.getComment());
        }
    }

    /**
     * 发送评论提醒消失给被回复用户
     */
    public void sendCommentNoticeMessage(Comment comment){
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
