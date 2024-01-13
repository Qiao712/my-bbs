package qiao.qasys.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import qiao.qasys.common.AddGroup;
import qiao.qasys.common.BaseEntity;
import qiao.qasys.common.UpdateGroup;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Null;

@Data
@TableName("t_authority")
@ApiModel(value = "Authority对象", description = "权限")
@EqualsAndHashCode(callSuper = true)
public class Authority extends BaseEntity {
    @Null(groups = {UpdateGroup.class, AddGroup.class}, message = "禁止修改权限标识")
    @ApiModelProperty("权限标识")
    private String authority;

    @ApiModelProperty("权限名称")
    @Length(groups = {AddGroup.class, UpdateGroup.class}, max = 50, message = "权限名称长度超出限制")
    private String name;

    @ApiModelProperty("描述")
    @Length(groups = {AddGroup.class, UpdateGroup.class}, max = 200, message = "描述长度超出限制")
    private String description;
    
    @ApiModelProperty("分类")
    @Length(groups = {AddGroup.class, UpdateGroup.class}, max = 20, message = "分类名长度超出限制")
    private String category;

    @ApiModelProperty("该权限是否有效(在@PreAuthorize中使用)")
    @Null(groups = {AddGroup.class, UpdateGroup.class})
    private Boolean valid;
}
