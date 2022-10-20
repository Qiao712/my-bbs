package github.qiao712.bbs.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@TableName("t_post_like")
@ApiModel(value = "PostLike对象", description = "用户对贴子的点赞记录")
@Data
public class PostLike implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long userId;

    private Long postId;

    private LocalDateTime createTime;
}
