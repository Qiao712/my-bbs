package github.qiao712.bbs.service;

import github.qiao712.bbs.domain.dto.CredentialDto;

/**
 * 认证服务
 */
public interface AuthenticationService {
    String login(CredentialDto credentialDto);

    void logout();
}
