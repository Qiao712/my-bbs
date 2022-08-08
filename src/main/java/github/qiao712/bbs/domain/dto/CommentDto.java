package github.qiao712.bbs.domain.dto;

import github.qiao712.bbs.domain.entity.Comment;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class CommentDto extends Comment {
    private UserDto author;
    private String repliedUserName;    //被回复的用户的用户
}
