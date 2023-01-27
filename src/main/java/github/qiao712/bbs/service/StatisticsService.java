package github.qiao712.bbs.service;

import java.time.LocalDateTime;
import java.util.List;

public interface StatisticsService {
    /**
     * 增加贴子浏览量
     */
    void increasePostViewCount(long postId);

    /**
     * 记录需要刷新热度的贴子id，等待刷新热度
     */
    void markPostToFreshScore(long postId);

    /**
     * 批量查询贴子浏览量
     */
    List<Long> listPostViewCounts(List<Long> postIds);

    /**
     * 保存缓存的贴子浏览量至数据库
     */
    void syncPostViewCount();

    /**
     * 刷新需要刷新热度分值的贴子的热度分值
     */
    void refreshPostScores();

    /**
     * 计算贴子的热度分值
     * 计算公式
     */
    Long computePostScore(long likeCount, long commentCount, long viewCount, LocalDateTime createTime);
}
