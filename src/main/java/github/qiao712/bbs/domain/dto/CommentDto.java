package github.qiao712.bbs.domain.dto;

import github.qiao712.bbs.domain.entity.Comment;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class CommentDto extends Comment {
    private UserDto author;            //通过用户查询列表时，不填充
    private String repliedUserName;    //被回复的用户的用户
    private Boolean liked;             //当前用户是否点赞

    //只在通过用户查询列表时填充
    private String postTitle;
    private Long forumId;
    private String forumName;
}
