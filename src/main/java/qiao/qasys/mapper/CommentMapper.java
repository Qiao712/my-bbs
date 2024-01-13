package qiao.qasys.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import qiao.qasys.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {
    Integer increaseLikeCount(Long commentId, Long delta);

    Boolean existsById(Long commentId);
}
