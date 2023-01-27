package github.qiao712.bbs;

import github.qiao712.bbs.schedule.SystemJob;
import org.junit.jupiter.api.Test;
import org.quartz.*;
import org.quartz.impl.JobExecutionContextImpl;
import org.quartz.impl.matchers.GroupMatcher;
import org.quartz.impl.triggers.SimpleTriggerImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;
import java.util.Set;

@SpringBootTest
public class TestQuartz {
    @Autowired
    private Scheduler scheduler;

    static class TestJob implements Job{
        @Override
        public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
            System.out.println("Job executed.");
        }
    }

    @Test
    public void clean() throws SchedulerException {
        scheduler.clear();
    }

    @Test
    public void test1() throws SchedulerException, InterruptedException {
        if(scheduler == null){
            System.out.println("Scheduler == null");
        }

        //情况
//        scheduler.clear();
//
        JobDetail jobDetail = JobBuilder.newJob(TestJob.class).withIdentity("test-job").build();
//
//        //每秒调用一次
//        Trigger trigger = TriggerBuilder.newTrigger().startAt(new Date()).withSchedule(CronScheduleBuilder.cronSchedule("* * * * * ? *")).build();
//
//        scheduler.scheduleJob(jobDetail, trigger);

        scheduler.start();
        Thread.sleep(1000000L);
    }

    @Test
    public void test2() throws SchedulerException {
        //JobKey即 job的group + name
        Set<JobKey> jobKeys = scheduler.getJobKeys(GroupMatcher.anyGroup());
        for (JobKey jobKey : jobKeys) {
            System.out.println(jobKey);
        }
    }
}
