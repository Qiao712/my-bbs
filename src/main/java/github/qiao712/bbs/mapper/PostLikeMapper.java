package github.qiao712.bbs.mapper;

import github.qiao712.bbs.domain.entity.PostLike;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface PostLikeMapper extends BaseMapper<PostLike> {
    Boolean isPostLikedByUser(Long postId, Long userId);

    Integer insertPostLikes(List<PostLike> postLikes);

    Integer deletePostLikes(List<PostLike> postLikes);
}
