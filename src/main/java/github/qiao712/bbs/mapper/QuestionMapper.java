package github.qiao712.bbs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import github.qiao712.bbs.domain.entity.Question;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface QuestionMapper extends BaseMapper<Question> {
    Integer updateLikeCount(Long questionId, Long likeCount);

    Integer increaseViewCount(Long questionId, Long delta);

    Integer increaseAnswerCount(Long questionId, Long delta);

    Long selectLikeCount(Long questionId);

    Long selectViewCount(Long questionId);

    List<Long> selectLikeCountBatch(List<Long> questionIds);

    List<Long> selectViewCountBatch(List<Long> questionIds);

    Integer updateScore(Long questionId, Long score);

    Boolean existsById(Long questionId);
}
