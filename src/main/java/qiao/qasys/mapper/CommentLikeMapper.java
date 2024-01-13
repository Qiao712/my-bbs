package qiao.qasys.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import qiao.qasys.entity.CommentLike;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CommentLikeMapper extends BaseMapper<CommentLike> {
    Boolean isCommentLikedByUser(Long commentId, Long userId);
}
