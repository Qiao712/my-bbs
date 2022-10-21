package github.qiao712.bbs.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MQConfig {
    public static final String POST_TOPIC = "post";
    public static final String POST_ADD_KEY = "post-add";
    public static final String POST_UPDATE_KEY = "post-update";
    public static final String POST_DELETE_KEY = "post-delete";

    public static final String COMMENT_TOPIC = "comment";
    public static final String COMMENT_ADD_KEY = "comment-add";


    private int numPartitions = 3;
    private int replicationFactor = 2;

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
