package github.qiao712.bbs.security;

import github.qiao712.bbs.domain.base.Result;
import github.qiao712.bbs.domain.base.ResultCode;
import github.qiao712.bbs.util.ResponseUtil;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class AuthenticationEntryPointImpl implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        Result<Void> result = Result.build(ResultCode.NO_PERMISSION, authException.getMessage());
        ResponseUtil.response(response, result);
    }
}
