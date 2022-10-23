package github.qiao712.bbs.mq;

import com.alibaba.fastjson.JSON;
import github.qiao712.bbs.domain.dto.PrivateMessageDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class ChatMessageSender {
    @Autowired
    KafkaTemplate<String, String> kafkaTemplate;

    /**
     * 将消息转发至其他节点
     */
    public void sendPrivateMessage(String chatServerId, PrivateMessageDto message){
        kafkaTemplate.send(chatServerId, JSON.toJSONString(message));
    }
}
