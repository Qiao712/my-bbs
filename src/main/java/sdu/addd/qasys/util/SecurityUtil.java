package sdu.addd.qasys.util;

import sdu.addd.qasys.dto.AuthUser;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtil {
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

    /**
     * 查询当前请求是否已认证(可获取当前用户)
     */
    public static boolean isAuthenticated(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getPrincipal() instanceof AuthUser;
    }
}
