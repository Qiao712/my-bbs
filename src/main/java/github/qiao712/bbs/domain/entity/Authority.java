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
import javax.validation.constraints.Null;

@Data
@TableName("t_authority")
@ApiModel(value = "Authority对象", description = "权限")
@EqualsAndHashCode(callSuper = true)
public class Authority extends BaseEntity {
    @Null(groups = {UpdateGroup.class, AddGroup.class}, message = "禁止修改权限标识")
    @ApiModelProperty("权限标识")
    private String authority;

    @ApiModelProperty("接口名称")
    private String name;

    @ApiModelProperty("描述")
    private String description;
    
    @ApiModelProperty("分类")
    private String category;
}
