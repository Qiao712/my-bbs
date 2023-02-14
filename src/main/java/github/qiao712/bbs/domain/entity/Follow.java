package github.qiao712.bbs.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import io.swagger.annotations.ApiModel;
import lombok.Data;

@Data
@TableName("t_follow")
@ApiModel(value = "Follow对象", description = "关注关系")
public class Follow implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long followerId;

    private Long followeeId;

    private LocalDateTime lastFeedTime;

    private LocalDateTime createTime;
}
