package sdu.addd.qasys.service;

import com.baomidou.mybatisplus.extension.service.IService;
import sdu.addd.qasys.entity.Authority;
import sdu.addd.qasys.entity.Role;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

public interface RoleService extends IService<Role> {
    //固定的角色
    final static String ROLE_ADMIN = "ROLE_ADMIN";
    final static String ROLE_ANONYMOUS = "ROLE_ANONYMOUS";

    List<Role> listRoles();

    Role getRole(Long roleId);

    boolean removeRole(Long roleId);

    boolean addRole(Role role);

    boolean updateRole(Role role);

    /**
     * 获取角色的权限列表
     * TokenFilter中，用于设置安全上下文的AuthenticationToken中的权限
     */
    List<SimpleGrantedAuthority> getGrantedAuthorities(String roleName);

    /**
     * 列出所有权限
     * 首次调用时扫描所有Controller获取@PreAuthorize中出现的权限，
     * 并将新的权限同步至数据库
     */
    List<Authority> listAuthorities();

    boolean updateAuthority(Authority authority);

    boolean removeAuthority(Long authorityId);
}
