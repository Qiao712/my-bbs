package github.qiao712.bbs.security;

import github.qiao712.bbs.domain.dto.AuthUser;
import github.qiao712.bbs.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
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

/**
 * 验证Token，并将用户信息保存至安全上下文
 */
@Component
public class TokenFilter extends OncePerRequestFilter{
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
                user.setAuthorities(roleService.getGrantedAuthorities(user.getRole()));
                Authentication authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);

                filterChain.doFilter(request, response);
                return;
            }
        }

        //未登录，设置为匿名Authentication
        List<SimpleGrantedAuthority> authorities = roleService.getGrantedAuthorities(RoleService.ROLE_ANONYMOUS);   //获取匿名用户的权限
        AnonymousAuthenticationToken authentication = new AnonymousAuthenticationToken("anonymous", "anonymousUser", authorities);  //角色名一定在Authorities中，不会为空
        SecurityContextHolder.getContext().setAuthentication(authentication);
        filterChain.doFilter(request, response);
    }
}
