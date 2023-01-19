package github.qiao712.bbs.mq.listener;

import github.qiao712.bbs.config.MQConfig;
import github.qiao712.bbs.domain.entity.Post;
import github.qiao712.bbs.exception.ServiceException;
import github.qiao712.bbs.mq.Message;
import github.qiao712.bbs.service.SearchService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.elasticsearch.ElasticsearchException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Post相关事件消息的监听器
 */
@Slf4j
@Component
public class PostMessageListener {
    @Autowired
    private SearchService searchService;

    @KafkaListener( topics = {MQConfig.POST_TOPIC},
                    errorHandler = "kafkaListenerErrorHandler")
    public void onMessage(ConsumerRecord<String, String> consumerRecord, Consumer<String, String> consumer){
        Message message = Message.parseMessage(consumerRecord);
        if(message == null){
            log.error("消息格式错误 {}", consumerRecord.value());
            consumer.commitSync();
            return;
        }

        try{
            switch (message.getType()){
                case POST_ADD: {
                    //贴子添加, 同步至ElasticSearch
                    log.debug("POST_ADD Message");
                    searchService.savePost((Post) message.getBody());
                    break;
                }

                case POST_UPDATE:{
                    //贴子更新, 同步至ElasticSearch
                    log.debug("POST_UPDATE Message");
                    searchService.updatePost((Post) message.getBody());
                    break;
                }

                case POST_DELETE:{
                    //贴子删除
                    log.debug("POST_DELETE Message");
                    searchService.removePost((Long) message.getBody());
                    break;
                }
            }
        } catch (ServiceException e){
            //业务错误不重试
            log.error("消息消费失败,抛弃" + consumerRecord.value(), e);
        } catch (RuntimeException | IOException e) {
            //ElasticSearch错误，不提交等待重试
            log.error("消息消费失败,等待重试 " + consumerRecord.value(), e);
            return;
        }

        consumer.commitSync();
    }
}
