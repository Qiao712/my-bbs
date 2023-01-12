package github.qiao712.bbs.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import github.qiao712.bbs.domain.entity.Authority;
import github.qiao712.bbs.domain.entity.Role;
import github.qiao712.bbs.domain.base.ResultCode;
import github.qiao712.bbs.exception.ServiceException;
import github.qiao712.bbs.mapper.AuthorityMapper;
import github.qiao712.bbs.mapper.RoleMapper;
import github.qiao712.bbs.service.RoleService;
import org.elasticsearch.common.util.set.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService{
    @Autowired
    private RoleMapper roleMapper;
    @Autowired
    private AuthorityMapper authorityMapper;
    @Autowired
    private ApplicationContext applicationContext;

    //缓存所有权限标识
    private static volatile boolean isAuthorityUpdated = false;
    //缓存角色拥有的权限列表
    private final ConcurrentMap<String, List<SimpleGrantedAuthority>> grantedAuthorityCache = new ConcurrentHashMap<>();

    @Override
    public List<Role> listRoles() {
        return roleMapper.selectList(null);
    }

    @Override
    public Role getRole(Long roleId) {
        return roleMapper.selectRole(roleId);
    }

    @Override
    public boolean removeRole(Long roleId) {
        Role role = roleMapper.selectById(roleId);
        if(role == null) return false;

        if(ROLE_ADMIN.equals(role.getName())){
            throw new ServiceException(ResultCode.ROLE_ERROR, "禁止删除ROLE_ADMIN");
        }else if(ROLE_ANONYMOUS.equals(role.getName())){
            throw new ServiceException(ResultCode.ROLE_ERROR, "禁止删除ROLE_ANONYMOUS");
        }

        roleMapper.revokeAllAuthorities(roleId);
        if(roleMapper.deleteById(roleId) > 0){
            //删除缓存
            grantedAuthorityCache.remove(role.getName());
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public boolean addRole(Role role) {
        boolean flag1 = roleMapper.insert(role) > 0;
        boolean flag2 = true;
        if(role.getAuthorities() != null && !role.getAuthorities().isEmpty()){
            flag2= roleMapper.grantAuthorities(role.getId(), role.getAuthorities()) > 0;
        }

        grantedAuthorityCache.remove(role.getName());

        if(flag1 && flag2) return true;
        else throw new ServiceException(ResultCode.ROLE_ERROR, "角色添加失败");  //抛出异常以回滚
    }

    @Override
    @Transactional
    public boolean updateRole(Role role) {
        Role originRole = roleMapper.selectRole(role.getId());
        if(role.getAuthorities() != null){
            Set<String> intersection = Sets.intersection(originRole.getAuthorities(), role.getAuthorities());
            Set<String> authoritiesToRevoke = Sets.difference(originRole.getAuthorities(), intersection);
            Set<String> authoritiesToGrant = Sets.difference(role.getAuthorities(), intersection);
            if(!authoritiesToGrant.isEmpty()) roleMapper.grantAuthorities(role.getId(), authoritiesToGrant);
            if(!authoritiesToRevoke.isEmpty()) roleMapper.revokeAuthorities(role.getId(), authoritiesToRevoke);
        }

        if(roleMapper.updateById(role) > 0){
            //删除缓存
            grantedAuthorityCache.remove(originRole.getName());
            return true;
        }else{
            throw new ServiceException(ResultCode.ROLE_ERROR, "角色更新失败");   //抛出异常以回滚
        }
    }

    @Override
    public List<SimpleGrantedAuthority> getGrantedAuthorities(String roleName) {
        List<SimpleGrantedAuthority> authorities = grantedAuthorityCache.get(roleName);

        if(authorities == null){
            Role role = roleMapper.selectRoleByName(roleName);
            if(role != null){
                authorities = role.getAuthorities().stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());

                //将角色名也添加进去
                authorities.add(new SimpleGrantedAuthority(role.getName()));

                grantedAuthorityCache.put(role.getName(), authorities);
            }else{
                grantedAuthorityCache.put("", Collections.emptyList());
            }
        }

        return authorities;
    }

    @Override
    @Transactional
    public List<Authority> listAuthorities() {
        //首次调用扫描全部Controller，更新权限数据
        if(!isAuthorityUpdated){
            synchronized (this){
                if(isAuthorityUpdated) return authorityMapper.selectList(null);

                //扫描出出现过的权限标识
                Set<String> authorityStrings = scanAuthorities();
                //将新的权限标识添加至数据库
                List<Authority> authorities = authorityMapper.selectList(null);
                Set<String> oldAuthorityStrings = authorities.stream().map(Authority::getAuthority).collect(Collectors.toSet());
                Set<String> newAuthorityStrings = Sets.difference(authorityStrings, oldAuthorityStrings);
                for (String newAuthorityString : newAuthorityStrings) {
                    Authority authority = new Authority();
                    authority.setAuthority(newAuthorityString);
                    authority.setValid(true);
                    authorityMapper.insert(authority);
                }

                //更新权限有效性字段
                //失效的权限(没有被扫描到)
                LambdaUpdateWrapper<Authority> updateWrapper = new LambdaUpdateWrapper<>();
                updateWrapper.set(Authority::getValid, false);
                updateWrapper.notIn(Authority::getAuthority, authorityStrings);
                authorityMapper.update(null, updateWrapper);
                //有效的权限(扫描到的)
                updateWrapper = new LambdaUpdateWrapper<>();
                updateWrapper.set(Authority::getValid, true);
                updateWrapper.in(Authority::getAuthority, authorityStrings);
                authorityMapper.update(null, updateWrapper);

                isAuthorityUpdated = true;
            }
        }

        return authorityMapper.selectList(null);
    }

    @Override
    public boolean updateAuthority(Authority authority) {
        return authorityMapper.updateById(authority) > 0;
    }

    @Override
    @Transactional
    public boolean removeAuthority(Long authorityId) {
        Authority authority = authorityMapper.selectById(authorityId);
        if(authority.getValid()){
            throw new ServiceException(ResultCode.ROLE_ERROR, "禁止删除未失效权限");
        }

        //取消所有关联
        roleMapper.revokeAuthorityOfAllRoles(authorityId);

        //清除缓存
        grantedAuthorityCache.clear();

        return authorityMapper.deleteById(authorityId) > 0;
    }

    /**
     * 扫描所有Controller中方法
     * 获取PreAuthorize 注解中的权限标识
     */
    private Set<String> scanAuthorities(){
        Set<String> authorities = new HashSet<>();

        Pattern pattern1 = Pattern.compile("hasAuthority\\('(.*?)'\\)");       //hasAuthority('....') ?-->非贪心地
        Pattern pattern2 = Pattern.compile("hasAnyAuthority\\((.*?)\\)");    //hasAnyAuthority(....)
        Map<String, Object> controllerBeans = applicationContext.getBeansWithAnnotation(Controller.class);
        for (Object controller : controllerBeans.values()) {
            Method[] methods = controller.getClass().getMethods();

            for (Method method : methods) {
                //从PreAuthorize的权限表达式中获取权限标识符
                PreAuthorize annotation = AnnotationUtils.findAnnotation(method, PreAuthorize.class);
                if(annotation == null) continue;
                String expression = annotation.value();

                //hasAuthority 中出现的权限标识
                Matcher matcher = pattern1.matcher(expression);
                while(matcher.find()){
                    authorities.add(matcher.group(1));
                }

                //hasAnyAuthority 中出现的权限标识
                matcher = pattern2.matcher(expression);
                while(matcher.find()){
                    String authorityString = matcher.group(1);
                    String[] split = authorityString.replace('\'', '\0').split(",");
                    for (String s : split) {
                        authorities.add(s.trim());
                    }
                }
            }
        }

        return authorities;
    }
}
