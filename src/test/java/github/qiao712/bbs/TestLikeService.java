package github.qiao712.bbs;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import github.qiao712.bbs.domain.entity.Comment;
import github.qiao712.bbs.domain.entity.Post;
import github.qiao712.bbs.domain.entity.PostLike;
import github.qiao712.bbs.mapper.CommentLikeMapper;
import github.qiao712.bbs.mapper.CommentMapper;
import github.qiao712.bbs.mapper.PostLikeMapper;
import github.qiao712.bbs.mapper.PostMapper;
import github.qiao712.bbs.service.LikeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@SpringBootTest
public class TestLikeService {
    @Autowired
    private LikeService likeService;

    @Autowired
    private PostLikeMapper postLikeMapper;
    @Autowired
    private PostMapper postMapper;
    @Autowired
    private CommentLikeMapper commentLikeMapper;
    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private final static String POST_LIKE_COUNT_TABLE = "post-like-counts";
    private final static String POST_LIKE_RECORD_TABLE = "post-like-records";
    private final static String COMMENT_LIKE_COUNT_TABLE = "comment-like-counts";
    private final static String COMMENT_LIKE_RECORD_TABLE = "comment-like-records";
    private final static String LIKED = "1";
    private final static String UNLIKED = "0";

    /**
     * 清空所有点赞
     * 重置到初始状态
     */
    @Test
    public void clearAllLike(){
        for(int i = 0; i < 100; i++){
            redisTemplate.delete(POST_LIKE_COUNT_TABLE + "-" + i);
            redisTemplate.delete(POST_LIKE_RECORD_TABLE + "-" + i);
            redisTemplate.delete(COMMENT_LIKE_COUNT_TABLE + "-" + i);
            redisTemplate.delete(COMMENT_LIKE_RECORD_TABLE + "-" + i);
        }

        LambdaUpdateWrapper<Comment> commentUpdate = new LambdaUpdateWrapper<>();
        commentUpdate.set(Comment::getLikeCount, 0);
        commentMapper.update(null, commentUpdate);
        commentLikeMapper.delete(null);

        LambdaUpdateWrapper<Post> postUpdate = new LambdaUpdateWrapper<>();
        postUpdate.set(Post::getLikeCount, 0);
        postMapper.update(null, postUpdate);
        postLikeMapper.delete(null);
    }

    @Test
    public void testSync2DB(){
        likeService.syncPostLikeCount();
    }

    @Test
    public void testConcurrentlyUpdateAField() throws InterruptedException {
        long begin = System.currentTimeMillis();

        int work = 50;
        int partSize = 1000/work;
        ExecutorService executorService = Executors.newFixedThreadPool(work);
        for(int i = 0; i < work; i++){
            int i_ = i;

            executorService.execute(()->{
                System.out.println("worker "+i_);
                for(int j = 0; j < partSize; j++){
                    postMapper.updateLikeCount(16L, (long) j);
                }
            });
        }
        executorService.shutdown();
        executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);

        long end = System.currentTimeMillis();
        System.out.println(end-begin + "ms");
        //9106ms
        //11351ms
    }

    @Test
    public void testConcurrentlyInsertPostLike() throws InterruptedException {
        long begin = System.currentTimeMillis();

        int work = 50;
        int partSize = 1000/work;
        ExecutorService executorService = Executors.newFixedThreadPool(work);
        for(int i = 0; i < work; i++){
            int i_ = i;

            executorService.execute(()->{
                System.out.println("worker "+i_);
                PostLike postLike = new PostLike();
                postLike.setPostId(21L);
                for(int j = 0; j < partSize; j++){
                    postLike.setUserId(i_ * partSize + j + 31L);
                    postLikeMapper.insert(postLike);
                }
            });
        }

        executorService.shutdown();
        executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
        long end = System.currentTimeMillis();
        System.out.println(end-begin + "ms");
        //10683ms
        //8419ms
    }
}
