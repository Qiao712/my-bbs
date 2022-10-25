package github.qiao712.bbs.domain.dto;

import github.qiao712.bbs.domain.entity.Comment;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CommentDetailDto extends Comment {
    private String questionTitle;
    private Long forumId;
    private String forum;
    private String authorUsername;
    private String repliedUserName;    //被回复的用户的用户
}
