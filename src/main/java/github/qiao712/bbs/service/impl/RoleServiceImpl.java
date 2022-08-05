package github.qiao712.bbs.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import github.qiao712.bbs.domain.entity.Role;
import github.qiao712.bbs.mapper.RoleMapper;
import github.qiao712.bbs.service.RoleService;
import org.springframework.stereotype.Service;

@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {
}
