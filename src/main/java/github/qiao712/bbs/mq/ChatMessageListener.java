package github.qiao712.bbs.mq;

import com.alibaba.fastjson.JSON;
import github.qiao712.bbs.domain.dto.PrivateMessageDto;
import github.qiao712.bbs.service.ChatService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * 用于接收从其他节点转发来的私聊信息
 */
@Component
public class ChatMessageListener{
    @Autowired
    private ChatService chatService;

    @KafkaListener(topics = "${sys.chatServerId}")  //使用定义的节点ID作为Topic名
    public void onMessage(ConsumerRecord<String, String> consumerRecord){
        PrivateMessageDto privateMessageDto = JSON.parseObject(consumerRecord.value(), PrivateMessageDto.class);
        chatService.sendMessage(privateMessageDto);
    }
}
