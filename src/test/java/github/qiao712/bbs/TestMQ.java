package github.qiao712.bbs;

import github.qiao712.bbs.mapper.PostMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class TestMQ {
    @Autowired
    private PostMapper postMapper;
}
