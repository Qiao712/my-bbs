package github.qiao712.bbs;

import github.qiao712.bbs.domain.entity.Post;
import github.qiao712.bbs.mapper.PostMapper;
import github.qiao712.bbs.service.FollowService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class TestFeed {
    @Autowired
    private FollowService followService;
    @Autowired
    private PostMapper postMapper;
}
