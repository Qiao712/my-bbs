package sdu.addd.qasys.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import sdu.addd.qasys.entity.Tag;
import sdu.addd.qasys.entity.TagRelation;

import java.util.List;

@Mapper
public interface TagRelationMapper extends BaseMapper<TagRelation> {

}
