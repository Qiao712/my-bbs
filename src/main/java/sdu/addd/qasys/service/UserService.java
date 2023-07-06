package sdu.addd.qasys.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import sdu.addd.qasys.common.PageQuery;
import sdu.addd.qasys.dto.UserDto;
import sdu.addd.qasys.entity.User;

import java.util.List;
import java.util.Set;

public interface UserService extends IService<User> {
    boolean register(User user);

    User getUser(Long userId);

    String getUsername(Long userId);

    boolean updateUser(User user);

    IPage<UserDto> listUsers(PageQuery pageQuery, UserDto condition);

    List<UserDto> listUsers(Set<Long> userIds);

    boolean removeUser(Long userId);

    boolean setUserStatus(Long userId, boolean enable);

    boolean setAvatar(Long userId, String url);
}
