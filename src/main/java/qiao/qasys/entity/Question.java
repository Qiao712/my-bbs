package qiao.qasys.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import qiao.qasys.common.AddGroup;
import qiao.qasys.common.BaseEntity;
import qiao.qasys.common.UpdateGroup;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.util.List;

@TableName("t_question")
@ApiModel(value = "问题对象", description = "问题")
@Data
@EqualsAndHashCode(callSuper = true)
public class Question extends BaseEntity {
    @ApiModelProperty("标题")
    @NotBlank(groups = AddGroup.class, message = "标题不可为空")
    @Length(max = 100, message = "标题长度超出限制")
    private String title;

    @ApiModelProperty("内容")
    @NotBlank(groups = AddGroup.class, message = "内容不可为空")
    @Length(groups = {AddGroup.class, UpdateGroup.class}, max = 655350000, message = "内容长度超出限制")
    private String content;

    @ApiModelProperty("发布者")
    @Null(groups = {AddGroup.class, UpdateGroup.class}, message = "不允许指定作者")
    private Long authorId;

    @ApiModelProperty("点赞数")
    @Null(groups = {AddGroup.class, UpdateGroup.class}, message = "不允许指定点赞数量")
    private Long likeCount;

    @ApiModelProperty("浏览量")
    @Null(groups = {AddGroup.class, UpdateGroup.class}, message = "不允许指定浏览量")
    private Long viewCount;

    @ApiModelProperty
    @Null(groups = {AddGroup.class, UpdateGroup.class}, message = "不允许指定评论数量")
    private Long answerCount;

    @ApiModelProperty("热度分值")
    @Null(groups = {AddGroup.class, UpdateGroup.class}, message = "不允许指定贴子热度分值")
    private Long score;

    @ApiModelProperty("绑定的标签")
    @NotNull(groups = {AddGroup.class, UpdateGroup.class}, message = "必须指定至少一个标签")
    @TableField(exist = false)
    private List<String> tags;
}