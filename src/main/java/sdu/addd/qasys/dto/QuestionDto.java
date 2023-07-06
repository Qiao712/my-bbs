package sdu.addd.qasys.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class QuestionDto {
    private Long id;
    private String title;
    private String content;
    private UserDto author;
    private String categoryName;
    private Long categoryId;
    private Long likeCount;
    private Long commentCount;
    private Boolean liked;              //当前用户是否点赞
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
