package github.qiao712.bbs.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import github.qiao712.bbs.domain.AddGroup;
import github.qiao712.bbs.domain.UpdateGroup;
import github.qiao712.bbs.domain.base.BaseEntity;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Null;

@TableName("t_user")
@ApiModel(value = "User对象", description = "用户表")
@Data
@EqualsAndHashCode(callSuper = true)
public class User extends BaseEntity {
    @NotBlank(groups = AddGroup.class, message = "用户名不可为空")
    @Length(min = 3, max = 16, groups = {AddGroup.class}, message = "用户名长度应在[3,6]间")
    @Null(groups = UpdateGroup.class, message = "禁止修改用户名")
    private String username;

    private Long roleId;

    @NotBlank(groups = AddGroup.class, message = "密码不可为空")
    @Length(min = 6, max = 16, groups = {AddGroup.class, UpdateGroup.class}, message = "密码长度应在[6,16]之间")
    private String password;

    private Boolean enable;

    @Null(groups = {AddGroup.class, UpdateGroup.class})
    private Long avatarFileId;

    @Email(groups = {AddGroup.class, UpdateGroup.class})
    private String email;

    private Boolean gender;

    @TableField(exist = false)
    private String role;
    @TableField(exist = false)
    private String avatarUrl;
}
