package github.qiao712.bbs.controller.admin;

import github.qiao712.bbs.domain.AddGroup;
import github.qiao712.bbs.domain.UpdateGroup;
import github.qiao712.bbs.domain.base.Result;
import github.qiao712.bbs.domain.dto.JobDto;
import github.qiao712.bbs.service.JobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/jobs")
public class JobAdminController {
    @Autowired
    private JobService jobService;

    @GetMapping("/{jobGroup}/{jobName}")
    @PreAuthorize("hasAuthority('admin:job:query')")
    public Result<JobDto> getJob(@PathVariable("jobGroup") String jobGroup, @PathVariable("jobName") String jobName){
        return Result.succeedNotNull(jobService.getJob(jobName, jobGroup));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('admin:job:query')")
    public Result<List<JobDto>> listJobs(String groupName){
        return Result.succeed(jobService.listJobs(groupName));
    }

    @DeleteMapping("/{jobGroup}/{jobName}")
    @PreAuthorize("hasAuthority('admin:job:remove')")
    public Result<Void> removeJob(@PathVariable("jobGroup") String jobGroup, @PathVariable("jobName") String jobName){
        jobService.removeJob(jobName, jobGroup);
        return Result.succeed();
    }

    @PostMapping
    @PreAuthorize("hasAuthority('admin:job:add')")
    public Result<Void> addJob(@RequestBody @Validated(AddGroup.class) JobDto jobDto){
        jobService.addJob(jobDto);
        return Result.succeed();
    }

    @PutMapping
    @PreAuthorize("hasAuthority('admin:job:update')")
    public Result<Void> updateJob(@RequestBody @Validated(UpdateGroup.class) JobDto jobDto){
        jobService.updateJob(jobDto);
        return Result.succeed();
    }

    @GetMapping("/{jobGroup}/{jobName}/run")
    @PreAuthorize("hasAuthority('admin:job:run')")
    public Result<Void> runJob(@PathVariable("jobGroup") String jobGroup, @PathVariable("jobName") String jobName){
        jobService.runJob(jobName, jobGroup);
        return Result.succeed();
    }

    @GetMapping("/{jobGroup}/{jobName}/pause")
    @PreAuthorize("hasAuthority('admin:job:state')")
    public Result<Void> pauseJob(@PathVariable("jobGroup") String jobGroup, @PathVariable("jobName") String jobName){
        jobService.pauseJob(jobName, jobGroup);
        return Result.succeed();
    }

    @GetMapping("/{jobGroup}/{jobName}/resume")
    @PreAuthorize("hasAuthority('admin:job:state')")
    public Result<Void> resumeJob(@PathVariable("jobGroup") String jobGroup, @PathVariable("jobName") String jobName){
        jobService.resumeJob(jobName, jobGroup);
        return Result.succeed();
    }
}
