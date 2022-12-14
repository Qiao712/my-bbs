package github.qiao712.bbs.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import github.qiao712.bbs.domain.AddGroup;
import github.qiao712.bbs.domain.UpdateGroup;
import github.qiao712.bbs.domain.base.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

@TableName("t_post")
@ApiModel(value = "Post对象", description = "贴子")
@Data
@EqualsAndHashCode(callSuper = true)
public class Post extends BaseEntity {
    @ApiModelProperty("标题")
    @NotBlank(groups = AddGroup.class, message = "标题不可为空")
    @Length(max = 100, message = "标题长度超出限制")
    private String title;

    @ApiModelProperty("内容")
    @NotBlank(groups = AddGroup.class, message = "内容不可为空")
    @Length(groups = {AddGroup.class, UpdateGroup.class}, max = 65535, message = "内容长度超出限制")
    private String content;

    @ApiModelProperty("发布者")
    @Null(groups = {AddGroup.class, UpdateGroup.class}, message = "不允许指定作者")
    private Long authorId;

    @ApiModelProperty("所属板块")
    @NotNull(groups = AddGroup.class, message = "所属板块不可为空")
    private Long forumId;

    @ApiModelProperty("点赞数")
    @Null(groups = {AddGroup.class, UpdateGroup.class}, message = "不允许指定点赞数量")
    private Long likeCount;

    @ApiModelProperty("浏览量")
    @Null(groups = {AddGroup.class, UpdateGroup.class}, message = "不允许指定浏览量")
    private Long viewCount;

    @ApiModelProperty
    @Null(groups = {AddGroup.class, UpdateGroup.class}, message = "不允许指定评论数量")
    private Long commentCount;

    @ApiModelProperty("热度分值")
    @Null(groups = {AddGroup.class, UpdateGroup.class}, message = "不允许指定贴子热度分值")
    private Long score;
}
