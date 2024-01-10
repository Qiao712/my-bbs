package sdu.addd.qasys.dto;

import lombok.Data;
import sdu.addd.qasys.entity.Tag;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class QuestionDto {
    private Long id;
    private String title;
    private String content;
    private UserDto author;
    private Long likeCount;
    private Long commentCount;
    private Boolean liked;              //当前用户是否点赞
    private List<Tag> tags;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
