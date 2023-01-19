package github.qiao712.bbs.mq.listener;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import github.qiao712.bbs.config.MQConfig;
import github.qiao712.bbs.domain.dto.message.ReplyMessageContent;
import github.qiao712.bbs.domain.entity.Comment;
import github.qiao712.bbs.domain.entity.Post;
import github.qiao712.bbs.mapper.CommentMapper;
import github.qiao712.bbs.mapper.PostMapper;
import github.qiao712.bbs.mq.Message;
import github.qiao712.bbs.service.MessageService;
import github.qiao712.bbs.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
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

    @KafkaListener( topics = {MQConfig.COMMENT_TOPIC},
                    errorHandler = "kafkaListenerErrorHandler")
    public void onMessage(ConsumerRecord<String, String> consumerRecord, Consumer<String, String> consumer){
        Message message = Message.parseMessage(consumerRecord);
        if(message == null){
            log.error("消息格式错误 {}", consumerRecord.value());
            consumer.commitSync();
            return;
        }

        switch (message.getType()){
            case COMMENT_ADD:{
                log.debug("COMMENT_ADD Message");
                sendCommentNoticeMessage((Comment) message.getBody());
                break;
            }
        }

        consumer.commitSync();
    }

    /**
     * 发送评论提醒消息
     */
    void sendCommentNoticeMessage(Comment comment){
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
            //使用评论的id作为消息的key，以便快速检索删除
            messageService.sendMessage(comment.getAuthorId(), receiverId, comment.getId().toString(), messageContent);
        }
    }
}
