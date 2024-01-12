package sdu.addd.qasys.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@TableName("t_conversation")
@ApiModel(value = "Conversation对象", description = "用户私信会话信息")
public class Conversation implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("规定两个用户ID中较小的放在user1ID中")
    private Long user1Id;

    @ApiModelProperty("规定两个用户ID中较大的放在user1ID中")
    private Long user2Id;

    @ApiModelProperty("最新一条消息")
    private Long lastMessageId;

    @ApiModelProperty("最后一条消息的时间")
    private LocalDateTime lastMessageTime;

    @ApiModelProperty("user1未读消息数量")
    private Integer unreadNum1;

    @ApiModelProperty("user2未读消息数量")
    private Integer unreadNum2;

    @TableField(exist = false)
    private Message lastMessage;
}
