package github.qiao712.bbs.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import github.qiao712.bbs.config.SystemProperties;
import github.qiao712.bbs.domain.base.PageQuery;
import github.qiao712.bbs.domain.dto.AuthUser;
import github.qiao712.bbs.domain.dto.UserDto;
import github.qiao712.bbs.domain.entity.Role;
import github.qiao712.bbs.domain.entity.User;
import github.qiao712.bbs.exception.ServiceException;
import github.qiao712.bbs.mapper.RoleMapper;
import github.qiao712.bbs.mapper.UserMapper;
import github.qiao712.bbs.service.FileService;
import github.qiao712.bbs.service.UserService;
import github.qiao712.bbs.util.FileUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
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
    private SystemProperties systemProperties;
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
        user.setRoleId(systemProperties.getDefaultRoleId());

        //加密存储密码
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", user.getUsername());
        if(userMapper.exists(queryWrapper)){
            throw new ServiceException("注册失败: 该用户名已被注册");
        }

        return userMapper.insert(user) > 0;
    }

    @Override
    public UserDto getUser(Long userId) {
        User user = userMapper.selectById(userId);
        Role role = roleMapper.selectById(user.getRoleId());
        user.setRole(role != null ? role.getName() : null);

        UserDto userDto = new UserDto();
        BeanUtils.copyProperties(user, userDto);
        return userDto;
    }

    @Override
    public IPage<UserDto> listUsers(PageQuery pageQuery, UserDto condition) {
        User user = new User();
        BeanUtils.copyProperties(condition, user);

        IPage<UserDto> page = pageQuery.getIPage();
        List<User> users = userMapper.selectUsers(page, user);
        List<UserDto> userDtos = users.stream().map(this::userDtoMap).collect(Collectors.toList());

        page.setRecords(userDtos);
        return page;
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
    public boolean setAvatar(Long userId, MultipartFile file) {
        if(file.getSize() > systemProperties.getMaxAvatarSize()){
            throw new ServiceException("头像图片大小超过" + systemProperties.getMaxAvatarSize() + "bytes");
        }
        if(!FileUtils.isPictureFile(file.getOriginalFilename())){
            throw new ServiceException("文件类型非法");
        }
        Long fileId = fileService.saveFile("user_avatar", file);

        User user = new User();
        user.setId(userId);
        user.setAvatarFileId(fileId);
        return userMapper.updateById(user) > 0;
    }

    private UserDto userDtoMap(User user){
        UserDto userDto = new UserDto();
        BeanUtils.copyProperties(user, userDto);
        return userDto;
    }
}
