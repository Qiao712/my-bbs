package github.qiao712.bbs.service.impl;

import github.qiao712.bbs.config.SystemConfig;
import github.qiao712.bbs.domain.dto.AuthUser;
import github.qiao712.bbs.domain.dto.CredentialDto;
import github.qiao712.bbs.domain.base.ResultCode;
import github.qiao712.bbs.exception.ServiceException;
import github.qiao712.bbs.security.TokenManager;
import github.qiao712.bbs.service.AuthenticationService;
import github.qiao712.bbs.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {
    @Autowired
    private SystemConfig systemConfig;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private TokenManager tokenManager;

    @Override
    public String login(CredentialDto credentialDto) {
        //通过用户名、密码进行认证获取新的Authentication
        Authentication authentication = new UsernamePasswordAuthenticationToken(credentialDto.getUsername(), credentialDto.getPassword());
        try{
            authentication = authenticationManager.authenticate(authentication);
        }catch (AuthenticationException e){
            String msg = "";
            if(e instanceof BadCredentialsException) msg = ": 用户名或密码错误";
            else if(e instanceof DisabledException) msg = ": 账户不可用";
            else if(e instanceof LockedException) msg = ": 账户被锁定";
            throw new ServiceException(ResultCode.LOGIN_ERROR, "登录失败" + msg);
        }

        boolean rememberMe = credentialDto.getRememberMe() != null && credentialDto.getRememberMe();
        long validTime = rememberMe ? systemConfig.getRememberMeTokenValidTime() : systemConfig.getTokenValidTime();

        AuthUser authUser = (AuthUser) authentication.getPrincipal();
        return tokenManager.createToken(authUser, validTime);
    }

    @Override
    public void logout() {
        AuthUser currentUser = SecurityUtil.getCurrentUser();
        tokenManager.removeToken(currentUser.getId());
    }
}
