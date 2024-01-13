package qiao.qasys.service;

import qiao.qasys.dto.CredentialDto;

/**
 * 认证服务
 */
public interface AuthenticationService {
    String login(CredentialDto credentialDto);

    void logout();
}
