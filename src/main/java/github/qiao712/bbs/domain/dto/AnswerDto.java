package github.qiao712.bbs.domain.dto;

import github.qiao712.bbs.domain.entity.Answer;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AnswerDto extends Answer {
    private UserDto author;
    private Boolean liked;  //当前用户是否点过
}
