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

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

@TableName("t_comment")
@ApiModel(value = "Comment对象", description = "评论")
@Data
@EqualsAndHashCode(callSuper = true)
public class Comment extends BaseEntity {
    @NotBlank(groups = {AddGroup.class, UpdateGroup.class}, message = "内容不许为空")
    @Length(groups = {AddGroup.class, UpdateGroup.class}, max = 1000, message = "评论长度超出限制")
    private String content;

    @Null(groups = {AddGroup.class, UpdateGroup.class}, message = "不允许指定作者")
    private Long authorId;

    @ApiModelProperty("所属回答id")
    @NotNull(groups = {AddGroup.class, UpdateGroup.class}, message = "必须指定所回复回答")
    private Long answerId;

    @ApiModelProperty("被回复的评论")
    private Long repliedId;

    @ApiModelProperty("点赞数")
    @Null(groups = {AddGroup.class, UpdateGroup.class}, message = "不允许指定点赞数量")
    private Integer likeCount;
}
