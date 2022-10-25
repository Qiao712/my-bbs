package github.qiao712.bbs.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import github.qiao712.bbs.domain.entity.Question;
import github.qiao712.bbs.exception.ServiceException;
import github.qiao712.bbs.mapper.QuestionMapper;
import github.qiao712.bbs.service.StatisticsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.*;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class StatisticsServiceImpl implements StatisticsService {
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private QuestionMapper questionMapper;

    //需要需要刷新热度分数的问题
    private final String QUESTION_SCORE_REFRESH_TABLE = "question_to_refresh";
    //浏览量统计，两个hash表轮换着使用
    private final String QUESTION_VIEW_COUNT_TABLE = "question_view_counts";
    //用于计算问题发布时间
    private final LocalDateTime QUESTION_EPOCH = LocalDateTime.of(2022, 7,12,0,0,0);

    @Override
    public void increaseQuestionViewCount(long questionId) {
        try{
            redisTemplate.opsForHash().increment(QUESTION_VIEW_COUNT_TABLE, String.valueOf(questionId), 1);
        }catch (RuntimeException e){
            //捕获所有异常防止其影响问题的获取
            log.error("问题浏览量增加失败", e);
        }
    }

    @Override
    public void markQuestionToFreshScore(long questionId) {
        try {
            redisTemplate.opsForSet().add(QUESTION_SCORE_REFRESH_TABLE, String.valueOf(questionId));
        }catch (RuntimeException e){
            log.error("标记问题热度分值需要更新失败", e);
        }
    }

    @Override
    public List<Long> listQuestionViewCounts(List<Long> questionIds) {
        List<Long> viewCounts = questionMapper.selectViewCountBatch(questionIds);
        if(viewCounts.size() != questionIds.size()){
            throw new ServiceException("包含无效QuestionId");
        }

        //redis中的一部分
        List<String> questionKeys = new ArrayList<>(questionIds.size());
        for (Long questionId : questionIds) {
            questionKeys.add(questionId.toString());
        }
        HashOperations<String, String, String> hashOps = redisTemplate.opsForHash();
        List<String> viewCountDeltas = hashOps.multiGet(QUESTION_VIEW_COUNT_TABLE, questionKeys);
        if(viewCounts.size() != viewCountDeltas.size()){
            throw new RuntimeException("从Redis中获取浏览量失败");
        }

        //加上redis中缓存的增量
        for(int i = 0; i < viewCounts.size(); i++){
            if(viewCountDeltas.get(i) != null){
                viewCounts.set(i, viewCounts.get(i) + Long.parseLong(viewCountDeltas.get(i)));
            }
        }

        return viewCounts;
    }

    @Override
    public void syncQuestionViewCount() {
        log.info("同步问题浏览量: 开始");

        final int BATCH_SIZE = 1000;    //收集多少条插入一次数据库
        List<Map.Entry<String, String>> entries = new ArrayList<>(BATCH_SIZE);

        BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(QUESTION_VIEW_COUNT_TABLE);
        try(Cursor<Map.Entry<String, String>> cursor = hashOps.scan(ScanOptions.scanOptions().count(BATCH_SIZE).build())){  //scan
            //收集一批
            while(cursor.hasNext()){
                for(int i = 0; i < BATCH_SIZE && cursor.hasNext(); i++){
                    Map.Entry<String, String> entry = cursor.next();
                    entries.add(entry);
                }

                //从Redis中删除
                Object[] keys = entries.stream().map(Map.Entry::getKey).toArray();
                hashOps.delete(keys);

                //同步至数据库
                for (Map.Entry<String, String> entry : entries) {
                    questionMapper.increaseViewCount(Long.parseLong(entry.getKey()), Long.parseLong(entry.getValue()));
                }
                entries.clear();
            }
        }

        log.info("同步问题浏览量: 完成");
    }


    @Override
    public void refreshQuestionScores(){
        log.info("问题热度刷新: 开始");

        final int BATCH_SIZE = 1000;
        List<Long> questionIds = new ArrayList<>(BATCH_SIZE);
        BoundSetOperations<String, String> setOps = redisTemplate.boundSetOps(QUESTION_SCORE_REFRESH_TABLE);

        try(Cursor<String> cursor = setOps.scan(ScanOptions.scanOptions().count(BATCH_SIZE).build())){
            if(cursor == null) {
                log.info("问题热度刷新: Redis游标获取失败");
                return;
            }

            while(cursor.hasNext()){
                //收集一批
                for(int i = 0; cursor.hasNext() && i < BATCH_SIZE; i++){
                    questionIds.add(Long.parseLong(cursor.next()));
                }

                //从Redis中删除
                Object[] keys = questionIds.stream().map(Object::toString).toArray();
                setOps.remove(keys);

                //重新计算并保存问题热度
                if(!questionIds.isEmpty()){
                    updateQuestionScore(questionIds);
                }
                questionIds.clear();
            }
        }

        log.info("问题热度刷新: 完成");
    }

    /**
     * 计算问题热度分数，并更新
     */
    @Override
    public void updateQuestionScore(List<Long> questionIds) {
        LambdaQueryWrapper<Question> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(Question::getId, Question::getCreateTime, Question::getLikeCount, Question::getViewCount, Question::getAnswerCount);
        queryWrapper.in(Question::getId, questionIds);
        List<Question> questions = questionMapper.selectList(queryWrapper);

        for (Question question : questions) {
            questionMapper.updateScore(question.getId(), computeQuestionScore(question.getLikeCount(), question.getAnswerCount(), question.getViewCount(), question.getCreateTime()));
        }
    }

    /**
     * 计算问题热度分值
     */
    @Override
    public Long computeQuestionScore(long likeCount, long commentCount, long viewCount, LocalDateTime createTime){
        //10个赞可以相当于1分钟
        //2个评论可以相当于1分钟
        return likeCount/10L + commentCount/2L + viewCount/30L + Duration.between(QUESTION_EPOCH, createTime).toMinutes();
    }
}
