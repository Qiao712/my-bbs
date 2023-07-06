package sdu.addd.qasys.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import sdu.addd.qasys.entity.AnswerLike;
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
