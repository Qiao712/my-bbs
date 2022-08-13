package github.qiao712.bbs;

import github.qiao712.bbs.service.LikeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;

@SpringBootTest
public class TestLikeService {
    @Autowired
    private LikeService likeService;

    @Test
    public void test(){
        likeService.likePost(1L);
    }

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Test
    public void testRedis(){
        List<Object> execute = stringRedisTemplate.execute(new SessionCallback<List<Object>>() {
            @Override
            @SuppressWarnings("unchecked")
            public List<Object> execute(RedisOperations operations) throws DataAccessException {
                operations.watch("test1");
//                operations.opsForValue().set("test1", "xxx");
                operations.multi();
                operations.opsForValue().set("test1", "123");
                operations.opsForValue().set("test2", "234");
                return operations.exec();
            }
        });

        System.out.println(execute.size());
        for (Object o : execute) {
            System.out.println(o);
        }
    }

    @Test
    public void incr(){
        Long increment = stringRedisTemplate.opsForValue().increment("test1");
        System.out.println(increment);
    }
}
