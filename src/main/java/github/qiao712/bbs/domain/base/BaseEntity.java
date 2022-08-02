package github.qiao712.bbs.domain.base;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import github.qiao712.bbs.domain.AddGroup;
import github.qiao712.bbs.domain.UpdateGroup;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class BaseEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    @Null(groups = AddGroup.class)
    @NotNull(groups = UpdateGroup.class)
    Long id;

    @Null(groups = {AddGroup.class, UpdateGroup.class})
    LocalDateTime createTime;

    @Null(groups = {AddGroup.class, UpdateGroup.class})
    LocalDateTime updateTime;
}
