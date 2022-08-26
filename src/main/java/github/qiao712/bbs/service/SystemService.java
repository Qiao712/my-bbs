package github.qiao712.bbs.service;

import github.qiao712.bbs.domain.dto.Statistic;

public interface SystemService {
    /**
     * 获取整个系统的统计信息
     * 贴子数量、评论数量、用户数量
     */
    Statistic getStatistic();
}
