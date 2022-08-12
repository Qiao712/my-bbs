package github.qiao712.bbs.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
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
import javax.validation.constraints.Null;

@TableName("t_forum")
@ApiModel(value = "Forum对象", description = "板块")
@Data
@EqualsAndHashCode(callSuper = true)
public class Forum extends BaseEntity {
    @NotBlank(groups = AddGroup.class, message = "名称不可为空")
    @Length(groups = {AddGroup.class, UpdateGroup.class}, min = 3, max = 10, message = "板块名称长度应在3到10之间")
    private String name;

    @Length(groups = {AddGroup.class, UpdateGroup.class}, min = 3, max = 100, message = "板块描述长度应在3到100之间")
    private String description;

    @ApiModelProperty("板块分类名")
    @NotBlank(groups = AddGroup.class, message = "分类名称不可为空")
    @Length(groups = {AddGroup.class, UpdateGroup.class}, min = 3, max = 10, message = "分类名称长度应在3到10之间")
    private String category;

    @ApiModelProperty("logo图片文件id")
    @Null(groups = {AddGroup.class, UpdateGroup.class}, message = "禁止直接指定logo图片id")
    private Long logoFileId;

    @TableField(exist = false)
    private String logoUrl;
}
