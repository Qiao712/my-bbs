package sdu.addd.qasys.dto;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
public class PrivateMessageDto {
    private Long receiverId;

    private Long senderId;

    private Integer type;

    private String content;

    private LocalDateTime createTime;
}
