package github.qiao712.bbs.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import github.qiao712.bbs.config.SystemConfig;
import github.qiao712.bbs.domain.base.PageQuery;
import github.qiao712.bbs.domain.dto.AuthUser;
import github.qiao712.bbs.domain.dto.UserDto;
import github.qiao712.bbs.domain.entity.FileIdentity;
import github.qiao712.bbs.domain.entity.Role;
import github.qiao712.bbs.domain.entity.User;
import github.qiao712.bbs.exception.ServiceException;
import github.qiao712.bbs.mapper.RoleMapper;
import github.qiao712.bbs.mapper.UserMapper;
import github.qiao712.bbs.service.FileService;
import github.qiao712.bbs.service.UserService;
import github.qiao712.bbs.util.SecurityUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
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
    @Autowired
    private FileService fileService;

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
            throw new ServiceException("注册失败: 该用户名已被注册");
        }

        //TODO: 邮箱注册功能
        user.setEmail(null);

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

        //头像图片url
        String avatarUrl = fileService.getFileUrl(user.getAvatarFileId());
        user.setAvatarUrl(avatarUrl);
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
    public Long getUserIdByUsername(String username) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, username);
        queryWrapper.select(User::getId);
        User user = userMapper.selectOne(queryWrapper);
        return user != null ? user.getId() : null;
    }

    @Override
    public boolean updateUser(User user) {
        AuthUser currentUser = SecurityUtil.getCurrentUser();
        if(currentUser.getId().equals(user.getId())){
            User originUser = userMapper.selectById(user.getId());
            if(originUser == null) return false;

            if(user.getEnable() != null && !Objects.equals(user.getEnable(), originUser.getEnable())){
                throw new ServiceException("不允许更改自己的用户状态");
            }

            if(user.getRoleId() != null && !Objects.equals(user.getRole(), originUser.getRole())){
                throw new ServiceException("不允许更改自己的角色");
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
        User user = userMapper.selectById(userId);
        if(user == null) return false;
        Long avatarFileId = user.getAvatarFileId();

        boolean flag = userMapper.deleteById(userId) > 0;

        //删除头像文件
        if(flag && avatarFileId != null){
            fileService.deleteFile(avatarFileId);
        }

        return flag;
    }

    @Override
    public boolean setUserStatus(Long userId, boolean enable) {
        User user = new User();
        user.setId(userId);
        user.setEnable(enable);
        return userMapper.updateById(user) > 0;
    }

    @Override
    @Transactional
    public boolean setAvatar(Long userId, Long fileId) {
        //检查图片文件上传来源
        FileIdentity fileIdentity = fileService.getFileIdentity(fileId);
        if(fileIdentity != null && !Objects.equals(fileIdentity.getSource(), FileService.USER_AVATAR_IMAGE_FILE)){
            throw new ServiceException("图片非法");
        }

        //释放原头像图片
        User originUser = userMapper.selectById(userId);
        if(originUser == null) return false;
        fileService.increaseReferenceCount(originUser.getAvatarFileId(), -1);

        //引用图片
        fileService.increaseReferenceCount(fileId, 1);

        User user = new User();
        user.setId(userId);
        user.setAvatarFileId(fileId);
        return userMapper.updateById(user) > 0;
    }

    private UserDto convertToUserDto(User user){
        UserDto userDto = new UserDto();
        BeanUtils.copyProperties(user, userDto);

        //查询头像url
        String avatarUrl = fileService.getFileUrl(user.getAvatarFileId());
        userDto.setAvatarUrl(avatarUrl);

        return userDto;
    }
}
