package github.qiao712.bbs.service;

import com.baomidou.mybatisplus.extension.service.IService;
import github.qiao712.bbs.domain.base.PageQuery;
import github.qiao712.bbs.domain.entity.Authority;
import github.qiao712.bbs.domain.entity.Role;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

public interface RoleService extends IService<Role> {
    List<Role> listRoles();

    Role getRole(Long roleId);

    boolean removeRole(Long roleId);

    boolean addRole(Role role);

    boolean updateRole(Role role);

    /**
     * 获取角色的权限列表
     */
    List<SimpleGrantedAuthority> getGrantedAuthorities(Long roleId);

    /**
     * 列出所有权限
     * 首次调用时扫描所有Controller获取@PreAuthorize中出现的权限，
     * 并将新的权限同步至数据库
     */
    List<Authority> listAuthorities();

    boolean updateAuthority(Authority authority);

    boolean removeAuthority(Long authorityId);
}
