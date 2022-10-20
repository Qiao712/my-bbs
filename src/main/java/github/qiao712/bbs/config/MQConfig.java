package github.qiao712.bbs.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rabbitmq.client.ConnectionFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.ReturnedMessage;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MQConfig {
    public static final String POST_EXCHANGE = "post";
    public static final String POST_ADD_QUEUE = "post-add";
    public static final String POST_UPDATE_QUEUE = "post-update";
    public static final String POST_DELETE_QUEUE = "post-delete";

    public static final String COMMENT_EXCHANGE = "comment";
    public static final String COMMENT_ADD_QUEUE = "comment-add";


    /**
     * 配置消息转换器
     */
    @Bean
    public MessageConverter messageConverter(){
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());  //添加对Java8 LocalDateTime的支持
        return new Jackson2JsonMessageConverter(objectMapper);
    }
}
