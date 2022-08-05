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

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Null;

@TableName("t_user")
@ApiModel(value = "User对象", description = "用户表")
@Data
@EqualsAndHashCode(callSuper = true)
public class User extends BaseEntity {
    private final static int MAX_USERNAME_LENGTH = 16;
    private final static int MIN_USERNAME_LENGTH = 3;
    private final static int MAX_PASSWORD_LENGTH = 16;
    private final static int MIN_PASSWORD_LENGTH = 6;

    @NotBlank(groups = AddGroup.class)
    @Length(min = MIN_USERNAME_LENGTH, max = MAX_USERNAME_LENGTH, groups = {AddGroup.class})
    @Null(groups = UpdateGroup.class)
    private String username;

    @Null(groups = AddGroup.class)
    private Long roleId;

    @NotBlank(groups = AddGroup.class)
    @Length(min = MIN_PASSWORD_LENGTH, max = MAX_PASSWORD_LENGTH, groups = {AddGroup.class, UpdateGroup.class})
    private String password;

    @Null(groups = AddGroup.class)
    private Boolean enable;

    @Null(groups = {AddGroup.class, UpdateGroup.class})
    private Long avatarFileId;


    @TableField(exist = false)
    private String role;
    @TableField(exist = false)
    private String avatarUrl;
}
