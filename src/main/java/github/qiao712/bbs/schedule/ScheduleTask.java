package github.qiao712.bbs.schedule;

import github.qiao712.bbs.service.FileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 定时任务
 */
@Component
@Slf4j(topic = "schedule")
public class ScheduleTask {
    @Autowired
    private FileService fileService;

    /**
     * 每日凌晨执行临时文件清理
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void clearTemporaryFile(){
        log.info("开始清理临时文件");
        fileService.clearTemporaryFile();
    }
}
