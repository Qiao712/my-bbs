package github.qiao712.bbs.mq;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import github.qiao712.bbs.config.MQConfig;
import github.qiao712.bbs.domain.dto.message.ReplyMessageContent;
import github.qiao712.bbs.domain.entity.Comment;
import github.qiao712.bbs.domain.entity.Question;
import github.qiao712.bbs.mapper.CommentMapper;
import github.qiao712.bbs.mapper.QuestionMapper;
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
    private QuestionMapper questionMapper;
    @Autowired
    private CommentMapper commentMapper;

    @KafkaListener(topics = {MQConfig.COMMENT_TOPIC})
    public void onMessage(ConsumerRecord<String, String> consumerRecord){
        CommentMessage commentMessage = JSON.parseObject(consumerRecord.value(), CommentMessage.class);

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
        LambdaQueryWrapper<Question> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Question::getId, comment.getPostId());
        queryWrapper.select(Question::getTitle, Question::getAuthorId);
        Question question = questionMapper.selectOne(queryWrapper);
        messageContent.setPostTitle(question.getTitle());

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
            receiverId = question.getAuthorId();
        }

        if(!Objects.equals(receiverId, comment.getAuthorId())){
            messageService.sendMessage(comment.getAuthorId(), receiverId, messageContent);
        }
    }
}
