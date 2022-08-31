package github.qiao712.bbs.domain.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户基本信息
 */
@Data
public class UserDto {
    private Long id;
    private String username;
    private Boolean enable;
    private String role;
    private String avatarUrl;
    private boolean gender;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
