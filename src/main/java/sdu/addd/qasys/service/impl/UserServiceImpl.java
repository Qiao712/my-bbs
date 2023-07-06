package sdu.addd.qasys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import sdu.addd.qasys.service.UserService;
import sdu.addd.qasys.config.SystemConfig;
import sdu.addd.qasys.common.PageQuery;
import sdu.addd.qasys.common.ResultCode;
import sdu.addd.qasys.dto.AuthUser;
import sdu.addd.qasys.dto.UserDto;
import sdu.addd.qasys.entity.Role;
import sdu.addd.qasys.entity.User;
import sdu.addd.qasys.exception.ServiceException;
import sdu.addd.qasys.mapper.RoleMapper;
import sdu.addd.qasys.mapper.UserMapper;
import sdu.addd.qasys.util.SecurityUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService, UserDetailsService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private RoleMapper roleMapper;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private SystemConfig systemConfig;

    /**
     * 实现UserDetailsService的方法，提供用户信息
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        User user = userMapper.selectOne(queryWrapper);
        Role role = roleMapper.selectById(user.getRoleId());
        user.setRole(role != null ? role.getName() : null);

        AuthUser authUser = new AuthUser();
        BeanUtils.copyProperties(user, authUser);

        return authUser;
    }

    @Override
    public boolean register(User user) {
        user.setRoleId(systemConfig.getDefaultRoleId());

        //加密存储密码
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", user.getUsername());
        if(userMapper.exists(queryWrapper)){
            throw new ServiceException(ResultCode.USER_ERROR, "该用户名已被注册");
        }

        return userMapper.insert(user) > 0;
    }

    @Override
    public User getUser(Long userId) {
        User user = userMapper.selectById(userId);
        if(user == null) return null;

        //role
        Role role = roleMapper.selectById(user.getRoleId());
        user.setRole(role != null ? role.getName() : null);
        user.setPassword(null);
        return user;
    }

    @Override
    public String getUsername(Long userId) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", userId);
        queryWrapper.select("username");
        User user = userMapper.selectOne(queryWrapper);
        return user != null ? user.getUsername() : null;
    }

    @Override
    public boolean updateUser(User user) {
        AuthUser currentUser = SecurityUtil.getCurrentUser();
        if(currentUser.getId().equals(user.getId())){
            User originUser = userMapper.selectById(user.getId());
            if(originUser == null) return false;

            if(user.getEnable() != null && !Objects.equals(user.getEnable(), originUser.getEnable())){
                throw new ServiceException(ResultCode.USER_ERROR, "不允许更改自己的用户状态");
            }

            if(user.getRoleId() != null && !Objects.equals(user.getRole(), originUser.getRole())){
                throw new ServiceException(ResultCode.USER_ERROR, "不允许更改自己的角色");
            }
        }

        if(user.getPassword() != null){
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        return userMapper.updateById(user) > 0;
    }

    @Override
    public IPage<UserDto> listUsers(PageQuery pageQuery, UserDto condition) {
        User user = new User();
        BeanUtils.copyProperties(condition, user);

        IPage<UserDto> page = pageQuery.getIPage();
        List<User> users = userMapper.selectUsers(page, user);
        List<UserDto> userDtos = users.stream().map(this::convertToUserDto).collect(Collectors.toList());

        page.setRecords(userDtos);
        return page;
    }

    @Override
    @Transactional
    public boolean removeUser(Long userId) {
        return userMapper.deleteById(userId) > 0;
    }

    @Override
    public boolean setUserStatus(Long userId, boolean enable) {
        User user = new User();
        user.setId(userId);
        user.setEnable(enable);
        return userMapper.updateById(user) > 0;
    }

    @Override
    public boolean setAvatar(Long userId, String imageUrl) {
        User user = new User();
        user.setId(userId);
        user.setAvatarUrl(imageUrl);
        return userMapper.updateById(user) > 0;
    }

    @Override
    public List<UserDto> listUsers(Set<Long> userIds) {
        if(userIds.isEmpty()) return Collections.emptyList();
        return userMapper.selectBatchIds(userIds).stream().map(this::convertToUserDto).collect(Collectors.toList());
    }

    private UserDto convertToUserDto(User user){
        UserDto userDto = new UserDto();
        BeanUtils.copyProperties(user, userDto);
        return userDto;
    }
}
