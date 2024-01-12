package sdu.addd.qasys.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import sdu.addd.qasys.common.AddGroup;
import sdu.addd.qasys.common.BaseEntity;
import sdu.addd.qasys.common.UpdateGroup;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.Length;

@TableName("t_notification")
@ApiModel(value = "Notification对象", description = "通知")
@Data
@EqualsAndHashCode(callSuper = true)
public class Notification extends BaseEntity {
    private Long receiverId;

    @ApiModelProperty("用于快速查找通知, 不同类型具有不同含义(例如:找到通过评论id找到其提示消息,以删除或修改)")
    private String notificationKey;

    @ApiModelProperty("通知类型")
    private String type;

    @ApiModelProperty("消息内容")
    @Length(groups = {AddGroup.class, UpdateGroup.class}, max = 1000, message = "消息长度超出限制")
    private String content;
}
