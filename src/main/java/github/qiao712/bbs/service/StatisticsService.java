package github.qiao712.bbs.service;

import java.time.LocalDateTime;
import java.util.List;

public interface StatisticsService {
    /**
     * 增加问题浏览量
     */
    void increaseQuestionViewCount(long questionId);

    /**
     * 记录需要刷新热度的问题id，等待刷新热度
     */
    void markQuestionToFreshScore(long questionId);

    /**
     * 批量查询问题浏览量
     */
    List<Long> listQuestionViewCounts(List<Long> questionIds);

    /**
     * 保存缓存的问题浏览量至数据库
     */
    void syncQuestionViewCount();

    /**
     * 刷新需要刷新热度分值的问题的热度分值
     */
    void refreshQuestionScores();


    /**
     * 更新指定问题的热度分值
     */
    void updateQuestionScore(List<Long> questionIds);

    /**
     * 计算问题的热度分值
     * 计算公式
     */
    Long computeQuestionScore(long likeCount, long commentCount, long viewCount, LocalDateTime createTime);
}
