package github.qiao712.bbs.schedule;

import github.qiao712.bbs.service.FileService;
import github.qiao712.bbs.service.LikeService;
import github.qiao712.bbs.service.StatisticsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 定时任务
 */
@Component
@Slf4j(topic = "schedule")
public class ScheduleTask {
    @Autowired
    private FileService fileService;
    @Autowired
    private StatisticsService statisticsService;
    @Autowired
    private LikeService likeService;

    /**
     * 每日凌晨执行临时文件清理
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void clearTemporaryFile(){
        log.info("开始清理临时文件");
        fileService.clearTemporaryFile();
    }

    /**
     * 每间隔一定时间同步贴子浏览量至数据库 并 重新计算贴子热度值
     */
    @Scheduled(fixedDelay = 3600, timeUnit = TimeUnit.SECONDS)
    public void refreshPost(){
        statisticsService.syncPostViewCount();
        statisticsService.refreshPostScores();
    }

    /**
     * 定时同步点赞量至数据库
     */
    @Scheduled(fixedDelay = 2, timeUnit = TimeUnit.HOURS)
    public void syncLikeCount(){
        likeService.syncPostLikeCount();
        likeService.syncCommentLikeCount();
    }
}
