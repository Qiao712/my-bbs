package github.qiao712.bbs.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import github.qiao712.bbs.domain.base.BaseEntity;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("t_answer")
@ApiModel(value = "Answer对象", description = "回答")
@EqualsAndHashCode(callSuper = true)
public class Answer extends BaseEntity {
    private Long questionId;

    private String content;

    private Long authorId;

    private Integer likeCount;
}
