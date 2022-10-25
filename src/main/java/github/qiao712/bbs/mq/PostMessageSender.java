package github.qiao712.bbs.mq;

import com.alibaba.fastjson.JSON;
import github.qiao712.bbs.config.MQConfig;
import github.qiao712.bbs.domain.entity.Question;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class PostMessageSender {
    @Autowired
    KafkaTemplate<String, String> kafkaTemplate;

    public void sendPostAddMessage(Question question){
        //使用PostId做为Key，使同一个Post的各种操作的消息进入同一个partition，保证对一个Post的操作的有序性
        PostMessage postAddMessage = PostMessage.buildPostAddMessage(question);
        kafkaTemplate.send(MQConfig.POST_TOPIC, question.getId().toString(), JSON.toJSONString(postAddMessage));
    }

    public void sendPostUpdateMessage(Question question){
        PostMessage postUpdateMessage = PostMessage.buildPostUpdateMessage(question);
        kafkaTemplate.send(MQConfig.POST_TOPIC, question.getId().toString(), JSON.toJSONString(postUpdateMessage));
    }

    public void sendPostDeleteMessage(Long postId){
        PostMessage postDeleteMessage = PostMessage.buildPostDeleteMessage(postId);
        kafkaTemplate.send(MQConfig.POST_TOPIC, postId.toString(), JSON.toJSONString(postDeleteMessage));
    }
}
