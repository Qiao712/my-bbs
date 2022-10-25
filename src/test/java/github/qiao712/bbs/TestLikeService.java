package github.qiao712.bbs;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import github.qiao712.bbs.domain.entity.Comment;
import github.qiao712.bbs.domain.entity.Question;
import github.qiao712.bbs.domain.entity.QuestionLike;
import github.qiao712.bbs.mapper.AnswerLikeMapper;
import github.qiao712.bbs.mapper.CommentMapper;
import github.qiao712.bbs.mapper.QuestionLikeMapper;
import github.qiao712.bbs.mapper.QuestionMapper;
import github.qiao712.bbs.service.LikeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@SpringBootTest
public class TestLikeService {
    @Autowired
    private LikeService likeService;

    @Autowired
    private QuestionLikeMapper questionLikeMapper;
    @Autowired
    private QuestionMapper questionMapper;
    @Autowired
    private AnswerLikeMapper answerLikeMapper;
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
        answerLikeMapper.delete(null);

        LambdaUpdateWrapper<Question> postUpdate = new LambdaUpdateWrapper<>();
        postUpdate.set(Question::getLikeCount, 0);
        questionMapper.update(null, postUpdate);
        questionLikeMapper.delete(null);
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
                    questionMapper.updateLikeCount(16L, (long) j);
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
                QuestionLike questionLike = new QuestionLike();
                questionLike.setQuestionId(21L);
                for(int j = 0; j < partSize; j++){
                    questionLike.setUserId(i_ * partSize + j + 31L);
                    questionLikeMapper.insert(questionLike);
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
