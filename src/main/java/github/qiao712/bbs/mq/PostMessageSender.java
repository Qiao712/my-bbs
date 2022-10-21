package github.qiao712.bbs.mq;

import com.alibaba.fastjson.JSON;
import github.qiao712.bbs.config.MQConfig;
import github.qiao712.bbs.domain.entity.Post;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class PostMessageSender {
    @Autowired
    KafkaTemplate<String, String> kafkaTemplate;

    public void sendPostAddMessage(Post post){
        //使用PostId做为Key，使同一个Post的各种操作的消息进入同一个partition，保证对一个Post的操作的有序性
        PostMessage postAddMessage = PostMessage.buildPostAddMessage(post);
        kafkaTemplate.send(MQConfig.POST_TOPIC, post.getId().toString(), JSON.toJSONString(postAddMessage));
    }

    public void sendPostUpdateMessage(Post post){
        PostMessage postUpdateMessage = PostMessage.buildPostUpdateMessage(post);
        kafkaTemplate.send(MQConfig.POST_TOPIC, post.getId().toString(), JSON.toJSONString(postUpdateMessage));
    }

    public void sendPostDeleteMessage(Long postId){
        PostMessage postDeleteMessage = PostMessage.buildPostDeleteMessage(postId);
        kafkaTemplate.send(MQConfig.POST_TOPIC, postId.toString(), JSON.toJSONString(postDeleteMessage));
    }
}
