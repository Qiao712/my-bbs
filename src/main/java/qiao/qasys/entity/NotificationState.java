package qiao.qasys.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import lombok.Data;

@TableName("t_notification_state")
@ApiModel(value = "NotificationState对象", description = "用户通知状态")
@Data
public class NotificationState {
    private Long userId;

    private String notificationType;

    private Boolean acknowledged;
}
