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

@TableName("t_message")
@ApiModel(value = "Message对象", description = "消息列表")
@Data
@EqualsAndHashCode(callSuper = true)
public class Message extends BaseEntity {
    @ApiModelProperty("发送者id(为空表示系统消息)")
    private Long senderId;

    private Long receiverId;

    @ApiModelProperty("用于快速查找消息, 不同类型具有不同含义(例如:找到通过评论id找到其提示消息,以删除或修改)")
    private String messageKey;

    @ApiModelProperty("消息类型")
    private String type;

    @ApiModelProperty("是否已读")
    private Boolean isAcknowledged;

    @ApiModelProperty("消息内容")
    @Length(groups = {AddGroup.class, UpdateGroup.class}, max = 500, message = "消息长度超出限制")
    private String content;
}
