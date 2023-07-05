package github.qiao712.bbs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import github.qiao712.bbs.domain.entity.AnswerLike;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 用户对答案的点赞记录 Mapper 接口
 * </p>
 *
 * @author qiao712
 * @since 2023-07-06
 */
@Mapper
public interface AnswerLikeMapper extends BaseMapper<AnswerLike> {
    Boolean isAnswerLikedByUser(Long answerId, Long userId);
}
