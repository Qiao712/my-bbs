package github.qiao712.bbs;

import github.qiao712.bbs.domain.entity.Post;
import github.qiao712.bbs.mapper.PostMapper;
import github.qiao712.bbs.service.StatisticsService;
import github.qiao712.bbs.util.DistributedLock;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootTest
public class TestStatisticsService {
    @Autowired
    private StatisticsService statisticsService;
    @Autowired
    private PostMapper postMapper;

    @Test
    public void testSyncPostViewCount(){
        statisticsService.syncPostViewCount();
    }

    @Test
    public void testListPostViewCounts(){
        List<Post> posts = postMapper.selectList(null);
        List<Long> postIds = posts.stream().map(Post::getId).collect(Collectors.toList());
        List<Long> counts = statisticsService.listPostViewCounts(postIds);
        for (Long count : counts) {
            System.out.println(count);
        }
    }

    @Test
    public void testRefreshPostScores(){
        statisticsService.refreshPostScores();
    }

    @Autowired
    RedisTemplate<String, Object> redisTemplate;
    @Autowired
    StringRedisTemplate stringRedisTemplate;
    @Test
    public void testLock() throws InterruptedException {
        DistributedLock distributedLock = new DistributedLock("testlock", 10, redisTemplate);
        System.out.println(distributedLock.tryLock() + "-------");
        System.out.println(distributedLock.tryLock() + "-------");
        System.out.println(distributedLock.tryLock() + "-------");
        System.out.println(distributedLock.tryLock() + "-------");

        for(int i = 0; i < 100; i++){
            distributedLock.refresh();
            System.out.println("续期");
            Thread.sleep(5000);
        }

        distributedLock.unlock();
    }

    @Test
    public void testLua(){
        DefaultRedisScript<Long> increaseIfExists = new DefaultRedisScript<>(
            "return redis.call(\"HEXISTS\", KEYS[1], ARGV[1])", Long.class
        );

        System.out.println("exists --------------------" + redisTemplate.execute(increaseIfExists, Collections.singletonList("testh"), 123));

        DefaultRedisScript<Long> set = new DefaultRedisScript<>(
                "return redis.call(\"HSET\", KEYS[1], ARGV[1], ARGV[2])", Long.class
        );

        System.out.println("set-----------------" + stringRedisTemplate.execute(set, Collections.singletonList("123123"), "444", "555"));
    }
}
