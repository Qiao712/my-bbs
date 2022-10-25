package github.qiao712.bbs.mq;

import com.alibaba.fastjson.JSON;
import github.qiao712.bbs.config.MQConfig;
import github.qiao712.bbs.domain.entity.Question;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class QuestionMessageSender {
    @Autowired
    KafkaTemplate<String, String> kafkaTemplate;

    public void sendQuestionAddMessage(Question question){
        //使用QuestionId做为Key，使同一个Question的各种操作的消息进入同一个partition，保证对一个Question的操作的有序性
        QuestionMessage questionAddMessage = QuestionMessage.buildQuestionAddMessage(question);
        kafkaTemplate.send(MQConfig.QUESTION_TOPIC, question.getId().toString(), JSON.toJSONString(questionAddMessage));
    }

    public void sendQuestionUpdateMessage(Question question){
        QuestionMessage questionUpdateMessage = QuestionMessage.buildQuestionUpdateMessage(question);
        kafkaTemplate.send(MQConfig.QUESTION_TOPIC, question.getId().toString(), JSON.toJSONString(questionUpdateMessage));
    }

    public void sendQuestionDeleteMessage(Long questionId){
        QuestionMessage questionDeleteMessage = QuestionMessage.buildQuestionDeleteMessage(questionId);
        kafkaTemplate.send(MQConfig.QUESTION_TOPIC, questionId.toString(), JSON.toJSONString(questionDeleteMessage));
    }
}
