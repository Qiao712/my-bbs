package github.qiao712.bbs.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import github.qiao712.bbs.domain.AddGroup;
import github.qiao712.bbs.domain.UpdateGroup;
import github.qiao712.bbs.domain.base.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

@TableName("t_comment")
@ApiModel(value = "Comment对象", description = "评论")
@Data
@EqualsAndHashCode(callSuper = true)
public class Comment extends BaseEntity {
    @NotBlank(groups = {AddGroup.class, UpdateGroup.class}, message = "内容不许为空")
    private String content;

    @Null(groups = {AddGroup.class, UpdateGroup.class}, message = "不允许指定作者")
    private Long authorId;

    @ApiModelProperty("所属的一级评论")
    @Null(groups = {AddGroup.class, UpdateGroup.class}, message = "不允许指定parentId")
    private Long parentId;

    @ApiModelProperty("被回复的评论")
    private Long repliedId;

    @ApiModelProperty("所属贴子id")
    @NotNull(groups = {AddGroup.class, UpdateGroup.class}, message = "必须指定所回复贴子")
    private Long postId;

    @ApiModelProperty("点赞数")
    @Null(groups = {AddGroup.class, UpdateGroup.class}, message = "不允许指定点赞数量")
    private Integer likeCount;
}
