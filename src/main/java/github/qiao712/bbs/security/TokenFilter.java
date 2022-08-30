package github.qiao712.bbs.security;

import github.qiao712.bbs.domain.base.Result;
import github.qiao712.bbs.domain.base.ResultStatus;
import github.qiao712.bbs.domain.dto.AuthUser;
import github.qiao712.bbs.service.RoleService;
import github.qiao712.bbs.util.ResponseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

/**
 * 验证Token，并将用户信息保存至安全上下文
 */
@Component
public class TokenFilter extends OncePerRequestFilter {
    @Autowired
    private TokenManager tokenManager;
    @Autowired
    private RoleService roleService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = request.getHeader("Token");

        //将用户信息保存至上下文
        if(token != null){
            AuthUser user = tokenManager.getUser(token);
            if(user != null){
                //设置权限信息
                user.setAuthorities(roleService.getGrantedAuthorities(user.getRoleId()));
                Authentication authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
//            else {
//                //Token以过期，返回提示
//                Result<Void> result = Result.build(ResultStatus.INVALID_TOKEN, "Token无效");
//                ResponseUtil.response(response, result);
//                return;
//            }
        }

        filterChain.doFilter(request, response);
    }
}
