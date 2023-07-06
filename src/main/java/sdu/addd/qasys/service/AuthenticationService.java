package sdu.addd.qasys.service;

import sdu.addd.qasys.dto.CredentialDto;

/**
 * 认证服务
 */
public interface AuthenticationService {
    String login(CredentialDto credentialDto);

    void logout();
}
