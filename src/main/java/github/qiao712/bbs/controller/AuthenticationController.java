package github.qiao712.bbs.controller;

import github.qiao712.bbs.config.SystemProperties;
import github.qiao712.bbs.domain.base.Result;
import github.qiao712.bbs.domain.dto.CredentialDto;
import github.qiao712.bbs.service.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {
    @Autowired
    private SystemProperties systemProperties;
    @Autowired
    private AuthenticationService authenticationService;

    @PostMapping("/login")
    public Result<Void> login(@Validated @RequestBody CredentialDto credentialDto, HttpServletResponse response){
        String token = authenticationService.login(credentialDto);

        Cookie cookie = new Cookie("Token", token);
        cookie.setHttpOnly(false);
        cookie.setPath("/");
        if((credentialDto.getRememberMe() != null) && credentialDto.getRememberMe()){
            cookie.setMaxAge(systemProperties.getRememberMeTokenValidTime());
        }
        response.addCookie(cookie);

        return Result.succeed("登录成功");
    }

    @GetMapping("/logout")
    public Result<Void> logout(){
        authenticationService.logout();
        return Result.succeed();
    }
}
