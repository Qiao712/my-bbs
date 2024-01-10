package sdu.addd.qasys.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;
import sdu.addd.qasys.entity.Tag;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface TagMapper extends BaseMapper<Tag> {
    @Select("SELECT * FROM t_tag WHERE id IN (SELECT tag_id FROM t_tag_relation WHERE question_id = #{questionId})")
    List<Tag> selectTagByQuestion(Long questionId);
}
