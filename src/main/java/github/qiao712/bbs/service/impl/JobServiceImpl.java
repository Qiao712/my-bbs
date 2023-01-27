package github.qiao712.bbs.service.impl;

import github.qiao712.bbs.domain.base.ResultCode;
import github.qiao712.bbs.domain.dto.JobDto;
import github.qiao712.bbs.exception.ServiceException;
import github.qiao712.bbs.schedule.SystemJob;
import github.qiao712.bbs.service.JobService;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.quartz.impl.triggers.CronTriggerImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class JobServiceImpl implements JobService {
    @Autowired
    private Scheduler scheduler;

    @Override
    public List<JobDto> listJobs(String groupName) {
        //匹配GroupName
        GroupMatcher<JobKey> matcher = (groupName == null || groupName.isEmpty()) ? GroupMatcher.anyJobGroup() : GroupMatcher.jobGroupContains(groupName);
        try {
            Set<JobKey> jobKeys = scheduler.getJobKeys(matcher);
            List<JobDto> jobDtos = new ArrayList<>(jobKeys.size());
            for (JobKey jobKey : jobKeys) {
                try {
                    jobDtos.add(getJob(jobKey));
                }catch (SchedulerException e){
                    //忽略无法正常获取的Job
                }
            }
            return jobDtos;
        } catch (SchedulerException e) {
            throw new ServiceException(ResultCode.JOB_ERROR, "JobDto获取列表失败", e);
        }
    }

    @Override
    public JobDto getJob(String jobName, String jobGroup) {
        try {
            return getJob(new JobKey(jobName, jobGroup));
        } catch (SchedulerException e) {
            throw new ServiceException(ResultCode.JOB_ERROR, "JobDto获取失败", e);
        }
    }

    @Override
    public void addJob(JobDto job) {
        //检查MisfirePolicy参数
        Integer misfirePolicy = job.getMisfirePolicy();
        if(misfirePolicy != CronTrigger.MISFIRE_INSTRUCTION_IGNORE_MISFIRE_POLICY
          && misfirePolicy != CronTrigger.MISFIRE_INSTRUCTION_FIRE_ONCE_NOW
          && misfirePolicy != CronTrigger.MISFIRE_INSTRUCTION_DO_NOTHING){
            throw new ServiceException(ResultCode.INVALID_PARAM, "MisfirePolicy不是合法的值");
        }
        //检查invokeTarget
        try{
            SystemJob.checkInvokeTarget(job.getInvokeTarget());
        }catch (IllegalArgumentException e){
            throw new ServiceException(ResultCode.INVALID_PARAM, "invokeTarget非法"+e.getMessage());
        }

        //创建Job
        JobKey jobKey = new JobKey(job.getJobName(), job.getJobGroup());
        JobDetail jobDetail = JobBuilder
                .newJob(SystemJob.class)
                .withIdentity(jobKey)
                .withDescription(job.getDescription())
                .storeDurably()
                .build();
        jobDetail.getJobDataMap().put(SystemJob.INVOKE_TARGET, job.getInvokeTarget());  //储存执行目标

        //创建CronTrigger
        CronTriggerImpl cronTrigger = new CronTriggerImpl();
        cronTrigger.setName(jobKey.getName() + "-trigger");
        cronTrigger.setGroup(jobKey.getGroup());
        cronTrigger.setMisfireInstruction(misfirePolicy);
        try {
            cronTrigger.setCronExpression(job.getCronExpression());
        } catch (ParseException e) {
            throw new ServiceException(ResultCode.JOB_ERROR, "Cron格式错误");
        }

        //添加任务
        try {
            if(scheduler.checkExists(jobKey)){
                throw new ServiceException(ResultCode.JOB_ERROR, String.format("Job(%s)已存在", jobKey));
            }
            scheduler.scheduleJob(jobDetail, cronTrigger);
        } catch (SchedulerException e) {
            throw new ServiceException(ResultCode.JOB_ERROR, "JobDto添加失败", e);
        }
    }

    @Override
    public void removeJob(String jobName, String jobGroup) {
        try {
            scheduler.deleteJob(new JobKey(jobName, jobGroup));
        } catch (SchedulerException e) {
            throw new ServiceException(ResultCode.JOB_ERROR, "JobDto移除失败", e);
        }
    }

    @Override
    public void updateJob(JobDto newJob) {
        JobKey jobKey = new JobKey(newJob.getJobName(), newJob.getJobGroup());

        try {
            if(!scheduler.checkExists(jobKey)){
                throw new ServiceException(ResultCode.JOB_ERROR, String.format("Job(%s)不存在", jobKey));
            }
        } catch (SchedulerException e) {
            throw new ServiceException(ResultCode.JOB_ERROR, e);
        }

        //newJob中为null的字段不变
        JobDto oldJob = getJob(newJob.getJobName(), jobKey.getGroup());
        if(!oldJob.getValid()){
            throw new ServiceException(ResultCode.JOB_ERROR, "无法直接修改该Job");
        }
        if(newJob.getDescription() == null) newJob.setDescription(oldJob.getDescription());
        if(newJob.getInvokeTarget() == null) newJob.setInvokeTarget(oldJob.getInvokeTarget());
        if(newJob.getCronExpression() == null) newJob.setCronExpression(oldJob.getCronExpression());
        if(newJob.getMisfirePolicy() == null) newJob.setMisfirePolicy(oldJob.getMisfirePolicy());

        //删除再创建
        removeJob(newJob.getJobName(), newJob.getJobGroup());
        addJob(newJob);
    }

    @Override
    public void runJob(String jobName, String jobGroup) {
        JobKey jobKey = new JobKey(jobName, jobGroup);
        try {
            scheduler.triggerJob(jobKey);
        } catch (SchedulerException e) {
            throw new ServiceException(ResultCode.JOB_ERROR, "执行失败", e);
        }
    }

    @Override
    public void pauseJob(String jobName, String jobGroup) {
        try {
            scheduler.pauseJob(new JobKey(jobName, jobGroup));
        } catch (SchedulerException e) {
            throw new ServiceException(ResultCode.JOB_ERROR, "暂停失败", e);
        }
    }

    @Override
    public void resumeJob(String jobName, String jobGroup) {
        try {
            scheduler.resumeJob(new JobKey(jobName, jobGroup));
        } catch (SchedulerException e) {
            throw new ServiceException(ResultCode.JOB_ERROR, "恢复失败", e);
        }
    }

    private JobDto getJob(JobKey jobKey) throws SchedulerException {
        JobDto jobDto = new JobDto();

        JobDetail jobDetail = scheduler.getJobDetail(jobKey);
        jobDto.setJobName(jobKey.getName());
        jobDto.setJobGroup(jobKey.getGroup());

        //JobClass为SystemJob的Job才可以正常显示
        if(! SystemJob.class.equals(jobDetail.getJobClass())){
            jobDto.setValid(false);
            return jobDto;
        }

        //获取job的trigger
        List<? extends Trigger> triggersOfJob = scheduler.getTriggersOfJob(jobKey);
        //仅关联了一个CronTrigger，才可以正常显示
        if(triggersOfJob.size() == 1 && triggersOfJob.get(0) instanceof CronTrigger){
            CronTrigger cronTrigger = (CronTrigger) triggersOfJob.get(0);
            jobDto.setValid(true);
            jobDto.setCronExpression(cronTrigger.getCronExpression());

            //misfire policy (含义由CronTrigger.MISFIRE_INSRUCTION_XXX定义)
            jobDto.setMisfirePolicy(cronTrigger.getMisfireInstruction());

            jobDto.setTriggerState(scheduler.getTriggerState(cronTrigger.getKey()));
        }else{
            jobDto.setValid(false); //标记为无法显示
            return jobDto;
        }

        jobDto.setInvokeTarget((String) jobDetail.getJobDataMap().get(SystemJob.INVOKE_TARGET));
        jobDto.setDescription(jobDetail.getDescription());
        return jobDto;
    }
}
