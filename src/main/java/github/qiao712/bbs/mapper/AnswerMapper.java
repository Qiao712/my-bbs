package github.qiao712.bbs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import github.qiao712.bbs.domain.entity.Answer;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 答案 Mapper 接口
 * </p>
 *
 * @author qiao712
 * @since 2023-07-05
 */
@Mapper
public interface AnswerMapper extends BaseMapper<Answer> {
    Integer increaseLikeCount(Long answerId, Long delta);

    Boolean existsById(Long answerId);
}
