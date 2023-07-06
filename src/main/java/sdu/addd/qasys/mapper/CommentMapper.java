package sdu.addd.qasys.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import sdu.addd.qasys.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {
    Integer increaseLikeCount(Long commentId, Long delta);

    Boolean existsById(Long commentId);
}
