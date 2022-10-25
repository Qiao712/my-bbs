package github.qiao712.bbs.mapper;

import github.qiao712.bbs.domain.entity.QuestionLike;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface QuestionLikeMapper extends BaseMapper<QuestionLike> {
    Boolean isQuestionLikedByUser(Long questionId, Long userId);

    Integer insertQuestionLikes(List<QuestionLike> questionLikes);

    Integer deleteQuestionLikes(List<QuestionLike> questionLikes);
}
