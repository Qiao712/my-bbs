package sdu.addd.qasys.service;

import java.time.LocalDateTime;
import java.util.List;

public interface StatisticsService {
    /**
     * 增加贴子浏览量
     */
    void increaseQuestionViewCount(long postId);

    /**
     * 记录需要刷新热度的贴子id，等待刷新热度
     */
    void markQuestionToFreshScore(long postId);

    /**
     * 保存缓存的贴子浏览量至数据库
     */
    void syncQuestionViewCount();

    /**
     * 刷新需要刷新热度分值的贴子的热度分值
     */
    void refreshQuestionScores();
    
    /**
     * 更新指定贴子的热度分值
     */
    void updateQuestionScore(List<Long> postIds);

    /**
     * 计算贴子的热度分值
     * 计算公式
     */
    Long computeQuestionScore(long likeCount, long commentCount, long viewCount, LocalDateTime createTime);
}
