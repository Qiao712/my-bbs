package github.qiao712.bbs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import github.qiao712.bbs.domain.entity.Post;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface PostMapper extends BaseMapper<Post> {
    Integer increaseLikeCount(Long postId, Long delta);

    Integer increaseViewCount(Long postId, Long delta);

    Integer increaseCommentCount(Long postId, Long delta);

    Long selectLikeCount(Long postId);

    Long selectViewCount(Long postId);

    List<Long> selectLikeCountBatch(List<Long> postIds);

    List<Long> selectViewCountBatch(List<Long> postIds);

    Integer updateScore(Long postId, Long score);
}
