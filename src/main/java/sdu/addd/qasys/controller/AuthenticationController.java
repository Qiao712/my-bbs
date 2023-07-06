package sdu.addd.qasys.controller;

import sdu.addd.qasys.common.Result;
import sdu.addd.qasys.dto.CredentialDto;
import sdu.addd.qasys.service.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {
    @Autowired
    private AuthenticationService authenticationService;

    @PostMapping("/login")
    public Result<String> login(@Validated @RequestBody CredentialDto credentialDto){
        String token = authenticationService.login(credentialDto);
        return Result.succeed("登录成功", token);
    }

    @GetMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> logout(){
        authenticationService.logout();
        return Result.succeed();
    }
}