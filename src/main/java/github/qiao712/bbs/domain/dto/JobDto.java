package github.qiao712.bbs.domain.dto;

import github.qiao712.bbs.domain.AddGroup;
import github.qiao712.bbs.domain.UpdateGroup;
import lombok.Data;
import org.quartz.Trigger;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

/**
 * Quartz定时任务
 * 当前仅支持关联了一个CronTrigger，JobClass为SystemJob的任务
 */
@Data
public class JobDto {
    /**
     * jobName和jobGroup 作为Job的主键
     */
    @NotBlank(groups = {AddGroup.class, UpdateGroup.class})
    private String jobName;
    @NotBlank(groups = {AddGroup.class, UpdateGroup.class})
    private String jobGroup;

    /**
     * 描述
     */
    private String description;

    /**
     * 调用目标
     * ”@beanName::method“
     * ”beanType::method“
     */
    @NotBlank(groups = {AddGroup.class})
    private String invokeTarget;

    /**
     * cron
     */
    @NotBlank(groups = {AddGroup.class})
    private String cronExpression;

    //含义由CronTrigger.MISFIRE_INSRUCTION_XXX定义
    // -1(MISFIRE_INSTRUCTION_IGNORE_MISFIRE_POLICY) : 补上错过的次数
    // 1(MISFIRE_INSTRUCTION_FIRE_ONCE_NOW): 立即执行一次
    // 2(MISFIRE_INSTRUCTION_DO_NOTHING): 等待下一次执行
    @NotNull(groups = {AddGroup.class})
    private Integer misfirePolicy;

    /**
     * Trigger状态
     */
    @Null
    private Trigger.TriggerState triggerState;

    /**
     * 是否符合格式（仅关联了一个CronTrigger，JobClass为SystemJob）
     */
    @Null(groups = {AddGroup.class, UpdateGroup.class})
    private Boolean valid;
}
