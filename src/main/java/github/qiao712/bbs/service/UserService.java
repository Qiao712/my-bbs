package github.qiao712.bbs.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import github.qiao712.bbs.domain.base.PageQuery;
import github.qiao712.bbs.domain.dto.UserDto;
import github.qiao712.bbs.domain.entity.User;
import org.springframework.web.multipart.MultipartFile;

public interface UserService extends IService<User> {
    boolean register(User user);

    User getUser(Long userId);

    String getUsername(Long userId);

    Long getUserIdByUsername(String username);

    boolean updateUser(User user);

    IPage<UserDto> listUsers(PageQuery pageQuery, UserDto condition);

    boolean removeUser(Long userId);

    boolean setUserStatus(Long userId, boolean enable);

    boolean setAvatar(Long userId, MultipartFile file);
}
