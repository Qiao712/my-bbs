package github.qiao712.bbs.domain.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PostDto {
    private Long id;
    private String title;
    private String content;
    private UserDto author;
    private String forumName;
    private Long forumId;
    private Long likeCount;
    private Long viewCount;
    private Boolean liked;              //当前用户是否点赞
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
