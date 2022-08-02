package github.qiao712.bbs.security;

import github.qiao712.bbs.domain.base.Result;
import github.qiao712.bbs.domain.base.ResultStatus;
import github.qiao712.bbs.domain.dto.AuthUser;
import github.qiao712.bbs.util.ResponseUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 验证Token，并将用户信息保存至安全上下文
 */
@Component
public class TokenFilter extends OncePerRequestFilter {
    @Autowired
    private TokenManager tokenManager;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = null;
        if(request.getCookies() != null){
            for (Cookie cookie : request.getCookies()) {
                if(cookie.getName().equals("Token")){
                    token = cookie.getValue();
                    break;
                }
            }
        }

        //将用户信息保存至上下文
        if(token != null){
            AuthUser user = tokenManager.getUser(token);
            if(user != null){
                Authentication authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }else {
                //Token以过期，返回提示
                Result<Void> result = Result.build(ResultStatus.INVALID_TOKEN, "Token无效");
                ResponseUtils.response(response, result);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}