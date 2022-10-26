package github.qiao712.bbs.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@TableName("t_question_like")
@ApiModel(value = "QuestionLike对象", description = "用户对问题的点赞记录")
@Data
public class QuestionLike implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long userId;

    private Long questionId;

    private LocalDateTime createTime;
}