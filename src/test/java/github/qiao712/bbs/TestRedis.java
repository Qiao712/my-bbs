package github.qiao712.bbs;

import github.qiao712.bbs.service.StatisticsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;

@SpringBootTest
public class TestRedis {
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Test
    public void testRedis(){
        //scan不能保证获取到刚刚添加的元素
        ScanOptions options = ScanOptions.scanOptions().count(2).build();
        Cursor<String> scan = redisTemplate.opsForSet().scan("test_set", options);
        scan.forEachRemaining(key->{
            System.out.println(key);

            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    @Autowired
    private StatisticsService statisticsService;
    @Test
    public void testGetPostViewCount() throws InterruptedException {
//        statisticsService.listPostViewCounts(Lists.list(1L, 3L, 4L, 6L));
//        for(int i = 0; i < 2902; i++){
//            redisTemplate.opsForHash().put("post_view_counts", String.valueOf(i), String.valueOf(i));
//        }

//        System.out.println("生成完成");
        new Thread(new Runnable() {
            @Override
            public void run() {
                for(int i = 0; i < 1020; i++){
                    redisTemplate.opsForHash().put("post_view_counts", String.valueOf(i), String.valueOf(i));
                }
            }
        }).start();

        Thread.sleep(10000);
//
//        statisticsService.syncPostViewCount();
    }
}
