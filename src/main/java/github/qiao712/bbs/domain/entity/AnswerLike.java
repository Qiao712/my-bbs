package github.qiao712.bbs.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@TableName("t_comment_like")
@ApiModel(value = "CommentLike对象", description = "用户对评论的点赞记录")
@Data
public class AnswerLike implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long userId;

    private Long answerId;

    private LocalDateTime createTime;
}
