package github.qiao712.bbs;

import com.fasterxml.jackson.core.JsonProcessingException;
import github.qiao712.bbs.domain.entity.Post;
import github.qiao712.bbs.mapper.PostMapper;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class TestMQ {
    @Autowired
    private AmqpTemplate amqpTemplate;

    @Autowired
    private PostMapper postMapper;

    @Test
    public void test() throws JsonProcessingException {
        Post post = postMapper.selectById(16);
        amqpTemplate.convertAndSend("post", "add", post);
        amqpTemplate.convertAndSend("post", "update", post);
        amqpTemplate.convertAndSend("post", "delete", 16L);
    }
}
