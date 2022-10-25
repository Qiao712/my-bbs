package github.qiao712.bbs.mq;

import com.alibaba.fastjson.JSON;
import github.qiao712.bbs.config.MQConfig;
import github.qiao712.bbs.service.SearchService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Question相关事件消息的监听器
 */
@Component
public class QuestionMessageListener {
    @Autowired
    private SearchService searchService;

    @KafkaListener(topics = {MQConfig.QUESTION_TOPIC})
    public void onMessage(ConsumerRecord<String, String> consumerRecord){
        QuestionMessage questionMessage = JSON.parseObject(consumerRecord.value(), QuestionMessage.class);

        switch (questionMessage.getQuestionMessageType()){
            case CREATE: {
                //问题添加, 同步至ElasticSearch
                searchService.saveQuestion(questionMessage.getQuestion());
                break;
            }

            case UPDATE:{
                //问题更新, 同步至ElasticSearch
                searchService.updateQuestion(questionMessage.getQuestion());
            }

            case DELETE:{
                //问题删除
                searchService.removeQuestion(questionMessage.getQuestionId());
            }
        }
    }
}
