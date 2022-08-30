package github.qiao712.bbs;

import github.qiao712.bbs.domain.entity.Authority;
import github.qiao712.bbs.domain.entity.Role;
import github.qiao712.bbs.service.RoleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.AntPathMatcher;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@SpringBootTest
public class TestSecurity {
    @Test
    public void notLogin(){
        AntPathMatcher antPathMatcher = new AntPathMatcher();
        System.out.println(antPathMatcher.match("/hello/{id}", "/hello/123"));
    }

    @Autowired
    private RoleService roleService;
    @Test
    public void testAuthority(){
        List<Authority> authorities = roleService.listAuthorities();
        for (Authority authority : authorities) {
            System.out.println(authority);
        }
    }

    @Test
    public void addAllAuthority(){
        Role role = new Role();
        role.setId(2L);
        Set<String> collect = roleService.listAuthorities().stream().map(Authority::getAuthority).collect(Collectors.toSet());
        role.setAuthorities(collect);

        roleService.updateRole(role);
    }
}
