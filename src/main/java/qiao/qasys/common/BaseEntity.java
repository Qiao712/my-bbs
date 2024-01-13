package qiao.qasys.common;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class BaseEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    @Null(groups = AddGroup.class, message = "新增时禁止设置主键")
    @NotNull(groups = UpdateGroup.class, message = "未指定目标实体的ID")
    Long id;

    @Null(groups = {AddGroup.class, UpdateGroup.class}, message = "不允许指定创建时间")
    @TableField(fill = FieldFill.INSERT)
    LocalDateTime createTime;

    @Null(groups = {AddGroup.class, UpdateGroup.class}, message = "不允许指定修改时间")
    @TableField(fill = FieldFill.UPDATE)
    LocalDateTime updateTime;
}
