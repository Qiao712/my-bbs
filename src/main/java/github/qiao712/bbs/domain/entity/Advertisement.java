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
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_advertisement")
@ApiModel(value = "Advertising对象", description = "首页广告")
public class Advertisement extends BaseEntity {
    @NotNull(groups = AddGroup.class)
    @NotBlank(groups = {UpdateGroup.class, AddGroup.class})
    @Length(groups = {AddGroup.class, UpdateGroup.class}, max = 300, message = "标题长度超出限制")
    private String title;

    @ApiModelProperty("跳转目标")
    @NotNull(groups = AddGroup.class)
    @NotBlank(groups = {UpdateGroup.class, AddGroup.class})
    @Length(groups = {AddGroup.class, UpdateGroup.class}, max = 300, message = "url长度超出限制")
    private String url;

    @NotNull(groups = AddGroup.class, message = "未设置广告图片")
    private Long imageFileId;

    @ApiModelProperty("顺序")
    private Integer sequence;

    @TableField(exist = false)
    @Null(groups = {UpdateGroup.class, AddGroup.class})
    private String imageUrl;
}
