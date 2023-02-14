package github.qiao712.bbs.config;

import java.time.LocalDateTime;

public class Constant {
    //PostService相关常量
    //贴子相关缓存key的前缀
    public static final String POST_KEY_PREFIX = "post-";                           //单个贴子缓存
    public static final String POST_LIST_BY_TIME_KEY_PREFIX = "post-list-time-";          //按时间排序的贴子列表缓存
    public static final String POST_LIST_BY_SCORE_KEY_PREFIX = "post-list-score-";        //按热度排序的贴子列表缓存

    //StatisticService相关常量
    public static final String POST_SCORE_REFRESH_TABLE = "post-to-refresh";  //需要需要刷新热度分数的贴子
    public static final String POST_VIEW_COUNT_TABLE = "post-view-counts";    //浏览量统计缓存
    public static final String USER_ACTIVE_BITMAP = "user-active-bitmap";     //用户活跃标记
    public static final LocalDateTime POST_EPOCH = LocalDateTime.of(2022, 7,12,0,0,0);    //用于计算贴子发布时间

    //LikeService相关常量
    public static final String POST_LIKE_COUNT_TABLE = "post-like-count-table";         //贴子点赞量缓存hash表
    public static final String COMMENT_LIKE_COUNT_TABLE = "comment-like-count-table";   //评论点赞量缓存hash表
    public static final int LIKE_COUNT_TABLE_NUM = 10;                                  //分表数量

    //FeedService相关常量
    public static final String OUTBOX_KEY_PREFIX = "outbox";
    public static final long OUTBOX_CACHE_EXPIRE_TIME = 1000 * 60 * 24 * 7;    //7days
}
