package github.qiao712.bbs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import github.qiao712.bbs.domain.entity.Attachment;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface AttachmentMapper extends BaseMapper<Attachment> {
    int insertAttachments(Long postId, Long commentId, List<Long> fileIds);

    List<Long> selectAttachmentFileIds(Long postId, Long commentId);
}
