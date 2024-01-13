package qiao.qasys.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import qiao.qasys.common.AddGroup;
import qiao.qasys.common.UpdateGroup;
import qiao.qasys.dto.UserDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 答案
 * </p>
 *
 * @author qiao712
 * @since 2023-07-05
 */
@Data
@TableName("t_answer")
@ApiModel(value = "Answer对象", description = "答案")
public class Answer implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @NotBlank(groups = {AddGroup.class, UpdateGroup.class}, message = "内容不许为空")
    @Length(groups = {AddGroup.class, UpdateGroup.class}, max = 1000000, message = "长度超出限制")
    private String content;

    @Null(groups = {AddGroup.class, UpdateGroup.class}, message = "不允许指定作者")
    private Long authorId;

    @ApiModelProperty("所属回答id")
    @NotNull(groups = {AddGroup.class, UpdateGroup.class}, message = "必须指定所问题")
    private Long questionId;

    @Null(groups = {AddGroup.class, UpdateGroup.class}, message = "不允许指定点赞数量")
    private Integer likeCount;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableField(exist = false)
    private Boolean liked;
    @TableField(exist = false)
    private UserDto author;
}
