package github.qiao712.bbs.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import github.qiao712.bbs.domain.base.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@TableName("t_message")
@ApiModel(value = "Message对象", description = "消息列表")
@Data
@EqualsAndHashCode(callSuper = true)
public class Message extends BaseEntity {
    @ApiModelProperty("发送者id(为空表示系统消息)")
    private Long senderId;

    private Long receiverId;

    @ApiModelProperty("用于表示两个用户间的会话，用于获取会话列表时groupBy.sender_id和receiver_id中，较大的放在高8字节，较小的放在低8字节。")
    private byte[] conversationId;

    @ApiModelProperty("消息类型")
    private String type;

    @ApiModelProperty("是否已读")
    private Boolean isAcknowledged;

    @ApiModelProperty("消息内容")
    private String content;
}
