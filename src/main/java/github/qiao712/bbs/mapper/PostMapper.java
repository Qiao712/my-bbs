package github.qiao712.bbs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import github.qiao712.bbs.domain.entity.Post;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface PostMapper extends BaseMapper<Post> {
    Integer getLikeCount(Long postId);

    List<Integer> getLikeCountBatch(List<Long> postIds);

    Integer increaseLikeCount(Long postId, Integer delta);
}
