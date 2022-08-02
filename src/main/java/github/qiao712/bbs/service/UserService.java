package github.qiao712.bbs.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import github.qiao712.bbs.domain.base.PageQuery;
import github.qiao712.bbs.domain.dto.UserDto;
import github.qiao712.bbs.domain.entity.User;
import org.springframework.web.multipart.MultipartFile;

/**
 * <p>
 * 用户表 服务类
 * </p>
 *
 * @author qiao712
 * @since 2022-07-25
 */
public interface UserService extends IService<User> {
    boolean register(User user);

    UserDto getUser(Long userId);

    IPage<UserDto> listUsers(PageQuery pageQuery, UserDto condition);

    boolean setUserStatus(Long userId, boolean enable);

    boolean setAvatar(Long userId, MultipartFile file);
}