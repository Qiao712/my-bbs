package github.qiao712.bbs.mq.post;

import com.alibaba.fastjson.JSON;
import github.qiao712.bbs.config.MQConfig;
import github.qiao712.bbs.service.SearchService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Post相关事件消息的监听器
 */
@Component
public class PostMessageListener {
    @Autowired
    private SearchService searchService;

    @KafkaListener(topics = {MQConfig.POST_TOPIC}, groupId = "post")
    public void onMessage(ConsumerRecord<String, String> consumerRecord){
        PostMessage postMessage = JSON.parseObject(consumerRecord.value(), PostMessage.class);
        processMessage(postMessage);
    }

    public void processMessage(PostMessage postMessage){
        switch (postMessage.getPostMessageType()){
            case CREATE: {
                //贴子添加, 同步至ElasticSearch
                searchService.savePost(postMessage.getPost());
                break;
            }

            case UPDATE:{
                //贴子更新, 同步至ElasticSearch
                searchService.updatePost(postMessage.getPost());
            }

            case DELETE:{
                //贴子删除
                searchService.removePost(postMessage.getPostId());
            }
        }
    }
}
