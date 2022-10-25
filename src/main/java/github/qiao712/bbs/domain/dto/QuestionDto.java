package github.qiao712.bbs.domain.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class QuestionDto {
    private Long id;
    private String title;
    private String content;
    private UserDto author;
    private Long adoptedAnswerId;
    private String forumName;
    private Long forumId;
    private Long likeCount;
    private Long answerCount;
    private Boolean liked;              //当前用户是否点赞
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private LocalDateTime lastAnswerTime;
}
