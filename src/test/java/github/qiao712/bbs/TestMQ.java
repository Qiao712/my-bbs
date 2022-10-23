package github.qiao712.bbs;

import com.alibaba.fastjson.JSON;
import github.qiao712.bbs.domain.dto.PrivateMessageDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;

@SpringBootTest
public class TestMQ {
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Test
    public void test(){
        PrivateMessageDto privateMessageDto = new PrivateMessageDto();
        privateMessageDto.setSenderId(123123L);
        privateMessageDto.setReceiverId(31L);
        privateMessageDto.setContent("test - mq");
        privateMessageDto.setType(1);
        kafkaTemplate.send("node-1", JSON.toJSONString(privateMessageDto));
    }
}
