package sdu.addd.qasys.schedule;

import sdu.addd.qasys.service.LikeService;
import sdu.addd.qasys.service.StatisticsService;
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
    private StatisticsService statisticsService;
    @Autowired
    private LikeService likeService;

    /**
     * 每间隔一定时间同步贴子浏览量至数据库 并 重新计算贴子热度值
     */
    @Scheduled(fixedDelay = 3600, timeUnit = TimeUnit.SECONDS)
    public void refreshPost(){
        statisticsService.syncQuestionViewCount();
        statisticsService.refreshQuestionScores();
    }
}
