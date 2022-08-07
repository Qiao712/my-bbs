package github.qiao712.bbs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import github.qiao712.bbs.domain.entity.FileIdentity;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface FileMapper extends BaseMapper<FileIdentity> {
    int updateTempFlag(List<Long> fileIds, boolean isTemporary);
}
