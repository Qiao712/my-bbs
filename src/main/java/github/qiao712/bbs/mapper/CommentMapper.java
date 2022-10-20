package github.qiao712.bbs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import github.qiao712.bbs.domain.dto.CommentDetailDto;
import github.qiao712.bbs.domain.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {
    IPage<CommentDetailDto> listCommentDetailDtos(IPage<?> page, Long authorId);

    Integer updateLikeCount(Long commentId, Long likeCount);

    Long selectLikeCount(Long commentId);

    Boolean existsById(Long commentId);
}
