package github.qiao712.bbs.service.impl;

import github.qiao712.bbs.domain.dto.Statistic;
import github.qiao712.bbs.mapper.CommentMapper;
import github.qiao712.bbs.mapper.PostMapper;
import github.qiao712.bbs.mapper.UserMapper;
import github.qiao712.bbs.service.SystemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SystemServiceImpl implements SystemService {
    @Autowired
    private PostMapper postMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private CommentMapper commentMapper;

    @Override
    public Statistic getStatistic() {
        Statistic statistic = new Statistic();
        statistic.setPostCount(postMapper.selectCount(null));
        statistic.setUserCount(userMapper.selectCount(null));
        statistic.setCommentCount(commentMapper.selectCount(null));
        return statistic;
    }
}
