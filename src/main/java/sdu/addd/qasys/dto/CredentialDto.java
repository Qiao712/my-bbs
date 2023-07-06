package sdu.addd.qasys.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 登录凭证
 */
@Data
public class CredentialDto {
    @NotBlank
    private String username;

    @NotBlank
    private String password;

    private Boolean rememberMe;
}
