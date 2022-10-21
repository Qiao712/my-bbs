package github.qiao712.bbs.mq;

import com.alibaba.fastjson.JSON;
import github.qiao712.bbs.config.MQConfig;
import github.qiao712.bbs.domain.entity.Comment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class CommentMessageSender {
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    public void sendCommentAddMessage(Comment comment){
        CommentMessage commentMessage = CommentMessage.buildCommentAddMessage(comment);
        kafkaTemplate.send(MQConfig.COMMENT_TOPIC, comment.getId().toString(), JSON.toJSONString(commentMessage));
    }
}
