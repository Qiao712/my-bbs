package github.qiao712.bbs.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MQConfig {
    public static final String POST_TOPIC = "post";
    public static final String COMMENT_TOPIC = "comment";

    public final static int numPartitions = 3;
    public final static int replicationFactor = 2;

    /**
     * 创建Topic
     */
    @Bean
    public NewTopic postTopic(){
        return new NewTopic(POST_TOPIC, numPartitions, (short) replicationFactor);
    }

    @Bean
    public NewTopic commentTopic(){
        return new NewTopic(COMMENT_TOPIC, numPartitions, (short) replicationFactor);
    }
}
