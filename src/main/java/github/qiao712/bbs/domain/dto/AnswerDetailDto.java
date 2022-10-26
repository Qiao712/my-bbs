package github.qiao712.bbs.domain.dto;

import github.qiao712.bbs.domain.entity.Answer;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 聚合了问题信息的答案对象
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AnswerDetailDto extends Answer {
    private Boolean liked;  //当前用户是否点过
    private String questionTitle;
}
