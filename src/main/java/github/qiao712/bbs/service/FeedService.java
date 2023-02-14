package github.qiao712.bbs.service;

import com.baomidou.mybatisplus.extension.service.IService;
import github.qiao712.bbs.domain.dto.PostDto;
import github.qiao712.bbs.domain.entity.Feed;
import github.qiao712.bbs.domain.entity.Post;

import java.util.List;

public interface FeedService extends IService<Feed> {
    /**
     * 列出动态列表
     */
    List<PostDto> listFeeds(Long followerId, Long after, Integer size);

    /**
     * 推送贴子
     * 将贴子推送给活跃用户
     */
    void pushFeed(Post post);
}
