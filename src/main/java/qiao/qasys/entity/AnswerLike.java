package qiao.qasys.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * <p>
 * 用户对答案的点赞记录
 * </p>
 *
 * @author qiao712
 * @since 2023-07-06
 */
@TableName("t_answer_like")
@ApiModel(value = "AnswerLike对象", description = "用户对答案的点赞记录")
public class AnswerLike implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long userId;

    private Long answerId;

    private LocalDateTime createTime;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
    public Long getAnswerId() {
        return answerId;
    }

    public void setAnswerId(Long answerId) {
        this.answerId = answerId;
    }
    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "AnswerLike{" +
            "userId=" + userId +
            ", answerId=" + answerId +
            ", createTime=" + createTime +
        "}";
    }
}
