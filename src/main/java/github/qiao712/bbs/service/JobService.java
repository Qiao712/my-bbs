package github.qiao712.bbs.service;

import github.qiao712.bbs.domain.dto.JobDto;

import java.util.List;

/**
 * 定时任务服务
 * 对Quartz定时任务进行设置
 * 仅支持关联了一个CronTrigger，JobClass为SystemJob的任务
 */
public interface JobService {
    /**
     * 列出定时任务
     * @param groupName 对groupName模糊匹配
     */
    List<JobDto> listJobs(String groupName);

    /**
     * 查询定时任务
     * 仅支持关联了一个CronTrigger，JobClass为SystemJob的任务
     */
    JobDto getJob(String jobName, String jobGroup);

    /**
     * 添加Job
     */
    void addJob(JobDto job);

    /**
     * 移除Job
     */
    void removeJob(String jobName, String jobGroup);

    /**
     * 更新Job(删除再添加)
     * jobName, jobGroup 作为主键查找job, 不可更新
     * 更新将会resume该job
     */
    void updateJob(JobDto job);

    /**
     * 立即运行
     */
    void runJob(String jobName, String jobGroup);

    /**
     * 暂停
     */
    void pauseJob(String jobName, String jobGroup);

    /**
     * 恢复
     */
    void resumeJob(String jobName, String jobGroup);
}
