package qiao.qasys;

import qiao.qasys.dto.CredentialDto;
import qiao.qasys.entity.User;
import qiao.qasys.exception.ServiceException;
import qiao.qasys.service.AuthenticationService;
import qiao.qasys.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

@SpringBootTest
public class TestUser {
    @Autowired
    private UserService userService;
    @Autowired
    private AuthenticationService authenticationService;

    /**
     * 批量注册
     */
    @Test
    public void generateUser(){
        int n = 1000;
        User user = new User();
        user.setPassword("12345678");
        user.setGender(true);
        for(int i = 0; i < n; i++){
            user.setUsername("user"+i);
            try{
                userService.register(user);
                user.setId(null);
            }catch (ServiceException e){
                System.out.println(e);
            }
        }
        System.out.println("Over");
    }

    /**
     * 批量登录，
     * 生成一个token的CSV文件
     */
    @Test
    public void login() throws IOException {
        int n = 1000;

        FileOutputStream fileOutputStream = new FileOutputStream("./tokens.csv");
        PrintWriter printWriter = new PrintWriter(fileOutputStream);
        printWriter.println("username, token");

        for(int i = 0; i < n; i++){
            CredentialDto credentialDto = new CredentialDto();
            credentialDto.setUsername("user"+i);
            credentialDto.setPassword("123456");
            credentialDto.setRememberMe(true);
            try{
                String token = authenticationService.login(credentialDto);
                printWriter.println(credentialDto.getUsername()+ ", " + token);
            }catch (ServiceException e){
                System.out.println(e);
            }
        }

        printWriter.close();
        fileOutputStream.close();
    }
}
