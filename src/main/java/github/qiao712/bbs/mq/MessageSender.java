package github.qiao712.bbs.mq;

import com.alibaba.fastjson.JSON;
import github.qiao712.bbs.domain.base.ResultCode;
import github.qiao712.bbs.exception.ServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.util.concurrent.ExecutionException;

@Slf4j
@Component
public class MessageSender {
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    /**
     * 同步发送消息
     * @throws ServiceException 失败时抛出
     */
    public void sendMessageSync(MessageType messageType, String key, Object body){
        //在Body前加消息类型名作为前缀
        String bodyJson = JSON.toJSONString(body);
        String data = messageType.name() + ";" + bodyJson;

        ListenableFuture<SendResult<String, String>> future = kafkaTemplate.send(messageType.getTopic(), key, data);

        try {
            future.get();
        } catch (InterruptedException | ExecutionException e) {
            String logMsg = String.format("Kafka消息发送失败(MessageType: %s, Topic: %s, Key: %s, Body: %s", messageType, messageType.getTopic(), key, body);
            log.error(logMsg, e);
            throw new ServiceException(ResultCode.FAILURE, e);
        }
    }

    /**
     * 异步发送消息
     */
    public void sendMessageAsync(MessageType messageType, String key, Object body){
        //在Body前加消息类型名作为前缀
        String bodyJson = JSON.toJSONString(body);
        String data = messageType.name() + ";" + bodyJson;

        ListenableFuture<SendResult<String, String>> future = kafkaTemplate.send(messageType.getTopic(), key, data);

        future.addCallback(new ListenableFutureCallback<SendResult<String, String>>() {
            @Override
            public void onFailure(Throwable ex) {
                String logMsg = String.format("Kafka消息发送失败(MessageType: %s, Topic: %s, Key: %s, Body: %s", messageType, messageType.getTopic(), key, body);
                log.error(logMsg, ex);
            }

            @Override
            public void onSuccess(SendResult<String, String> result) {

            }
        });
    }
}
