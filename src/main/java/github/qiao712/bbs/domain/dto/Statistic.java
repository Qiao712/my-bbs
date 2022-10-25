package github.qiao712.bbs.domain.dto;

import lombok.Data;

/**
 * 统计信息
 */
@Data
public class Statistic {
    private long questionCount;     //问题总量
    private long commentCount;  //评论总量
    private long userCount;     //用户总量
}
