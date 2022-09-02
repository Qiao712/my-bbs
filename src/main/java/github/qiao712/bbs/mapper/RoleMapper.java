package github.qiao712.bbs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import github.qiao712.bbs.domain.entity.Authority;
import github.qiao712.bbs.domain.entity.Role;
import org.apache.ibatis.annotations.Mapper;

import java.util.Set;

@Mapper
public interface RoleMapper extends BaseMapper<Role> {
    Role selectRole(Long id);

    Role selectRoleByName(String roleName);

    Integer grantAuthorities(Long roleId, Set<String> authorities);

    Integer revokeAuthorities(Long roleId, Set<String> authorities);

    Integer revokeAllAuthorities(Long roleId);

    Integer revokeAuthorityOfAllRoles(Long authorityId);
}
