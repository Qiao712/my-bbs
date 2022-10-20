package github.qiao712.bbs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import github.qiao712.bbs.domain.entity.CommentLike;
import github.qiao712.bbs.domain.entity.PostLike;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CommentLikeMapper extends BaseMapper<CommentLike> {
    Boolean isCommentLikedByUser(Long commentId, Long userId);

    Integer insertCommentLikes(List<CommentLike> commentLikes);

    Integer deleteCommentLikes(List<CommentLike> commentLikes);
}
