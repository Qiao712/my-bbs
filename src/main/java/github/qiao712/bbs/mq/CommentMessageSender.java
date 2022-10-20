package github.qiao712.bbs.mq;

import github.qiao712.bbs.config.MQConfig;
import github.qiao712.bbs.domain.entity.Comment;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CommentMessageSender {
    @Autowired
    private AmqpTemplate amqpTemplate;

    public void sendCommentAddMessage(Comment comment){
        amqpTemplate.convertAndSend(MQConfig.COMMENT_EXCHANGE, MQConfig.COMMENT_ADD_QUEUE, comment);
    }
}
