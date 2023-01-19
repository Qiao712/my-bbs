package github.qiao712.bbs.mq;

import com.alibaba.fastjson.JSON;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;

@Data
@AllArgsConstructor
public class Message {
    private MessageType type;
    private Object body;

    /**
     * 解析转换Kafka中的消息格式
     * @return 格式错误解析失败，返回null
     */
    public static Message parseMessage(ConsumerRecord<String, String> consumerRecord){
        String value = consumerRecord.value();
        MessageType messageType = null;
        Object body = null;

        try{
            int p = value.indexOf(';');
            messageType = MessageType.valueOf(value.substring(0, p));
            body = JSON.parseObject(value.substring(p+1), messageType.getMessageBodyType());
        }catch (Throwable e){
            return null;
        }

        return new Message(messageType, body);
    }
}
