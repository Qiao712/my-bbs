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

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_advertising")
@ApiModel(value = "Advertising对象", description = "首页广告")
public class Advertisement extends BaseEntity {
    @NotNull(groups = AddGroup.class)
    @NotBlank(groups = {UpdateGroup.class, AddGroup.class})
    private String title;

    @ApiModelProperty("跳转目标")
    @NotNull(groups = AddGroup.class)
    @NotBlank(groups = {UpdateGroup.class, AddGroup.class})
    private String url;

    @NotNull(groups = AddGroup.class)
    @NotBlank(groups = {UpdateGroup.class, AddGroup.class})
    private Long imageFileId;

    @ApiModelProperty("顺序")
    private Integer order;

    @TableField(exist = false)
    @Null(groups = {UpdateGroup.class, AddGroup.class})
    private String imageUrl;
}
