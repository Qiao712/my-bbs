package github.qiao712.bbs.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("t_private_message")
@ApiModel(value = "PrivateMessage对象", description = "用户私信")
public class PrivateMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long senderId;

    private Long receiverId;

    private Long conversationId;

    @ApiModelProperty("消息的类型(0:文本消息)")
    private Integer type;

    private Boolean isAcknowledged;

    private String content;

    @ApiModelProperty("即发送时间")
    private LocalDateTime createTime;
}
