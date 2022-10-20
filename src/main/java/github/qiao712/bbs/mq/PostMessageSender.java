package github.qiao712.bbs.mq;

import github.qiao712.bbs.config.MQConfig;
import github.qiao712.bbs.domain.entity.Post;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PostMessageSender {
    @Autowired
    AmqpTemplate amqpTemplate;

    public void sendPostAddMessage(Post post){
        amqpTemplate.convertAndSend(MQConfig.POST_EXCHANGE, MQConfig.POST_ADD_QUEUE, post);
    }

    public void sendPostUpdateMessage(Post post){
        amqpTemplate.convertAndSend(MQConfig.POST_EXCHANGE, MQConfig.POST_UPDATE_QUEUE, post);
    }

    public void sendPostDeleteMessage(Long postId){
        amqpTemplate.convertAndSend(MQConfig.POST_EXCHANGE, MQConfig.POST_DELETE_QUEUE, postId);
    }
}
