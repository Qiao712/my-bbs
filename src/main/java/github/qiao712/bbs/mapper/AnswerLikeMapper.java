package github.qiao712.bbs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import github.qiao712.bbs.domain.entity.AnswerLike;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface AnswerLikeMapper extends BaseMapper<AnswerLike> {
    Boolean isAnswerLikedByUser(Long answerId, Long userId);

    Integer insertAnswerLikes(List<AnswerLike> answerLikes);

    Integer deleteAnswerLikes(List<AnswerLike> answerLikes);
}
