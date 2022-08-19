package github.qiao712.bbs.domain.dto;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class PrivateMessageDto {
    @NotNull
    private Long receiverId;

    @NotBlank
    @Length(min = 1, max = 500)
    private String text;
}
