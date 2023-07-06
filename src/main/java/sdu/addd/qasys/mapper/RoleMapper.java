package sdu.addd.qasys.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import sdu.addd.qasys.entity.Role;
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
