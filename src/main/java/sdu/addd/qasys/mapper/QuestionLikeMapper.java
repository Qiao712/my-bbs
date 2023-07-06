package sdu.addd.qasys.mapper;

import sdu.addd.qasys.entity.QuestionLike;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface QuestionLikeMapper extends BaseMapper<QuestionLike> {
    Boolean isQuestionLikedByUser(Long postId, Long userId);
}