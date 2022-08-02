package github.qiao712.bbs.util;

import github.qiao712.bbs.domain.dto.AuthUser;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {
    /**
     * 获取当前用户
     * @return AuthUser
     * @throws AccessDeniedException 未登录则抛出
     */
    public static AuthUser getCurrentUser(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication.getPrincipal() instanceof AuthUser){
            return (AuthUser) authentication.getPrincipal();
        }else{
            throw new AccessDeniedException("未登录");
        }
    }
}
