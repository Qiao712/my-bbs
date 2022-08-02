package github.qiao712.bbs.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import github.qiao712.bbs.domain.base.BaseEntity;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

@TableName("t_role")
@ApiModel(value = "Role对象", description = "角色表")
@Data
@EqualsAndHashCode(callSuper = true)
public class Role extends BaseEntity {
    private String name;
}
