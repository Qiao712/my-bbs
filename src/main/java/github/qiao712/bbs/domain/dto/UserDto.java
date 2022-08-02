package github.qiao712.bbs.domain.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户基本信息
 */
@Data
public class UserDto {
    private String username;
    private String role;
    private Boolean enable;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
