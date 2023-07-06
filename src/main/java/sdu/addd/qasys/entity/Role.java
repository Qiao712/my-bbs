package sdu.addd.qasys.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import sdu.addd.qasys.common.AddGroup;
import sdu.addd.qasys.common.BaseEntity;
import sdu.addd.qasys.common.UpdateGroup;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.util.Set;

@TableName("t_role")
@ApiModel(value = "Role对象", description = "角色表")
@Data
@EqualsAndHashCode(callSuper = true)
public class Role extends BaseEntity {
    @Null(groups = UpdateGroup.class, message = "禁止修改角色名")
    @NotNull(groups = AddGroup.class)
    @Length(groups = {AddGroup.class, UpdateGroup.class}, max = 32, message = "角色名长度超出限制")
    private String name;

    @TableField(exist = false)
    private Set<String> authorities;
}
