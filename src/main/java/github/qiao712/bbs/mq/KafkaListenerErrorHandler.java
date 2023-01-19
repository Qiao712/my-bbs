package github.qiao712.bbs.mq;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.kafka.listener.ListenerExecutionFailedException;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class KafkaListenerErrorHandler implements org.springframework.kafka.listener.KafkaListenerErrorHandler {
    @NotNull
    @Override
    public Object handleError(@NotNull Message<?> message, @NotNull ListenerExecutionFailedException exception) {
        log.error("消息消费失败" + message.getPayload(), exception);
        return null;
    }
}
