package github.qiao712.bbs.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import github.qiao712.bbs.domain.entity.Message;
import github.qiao712.bbs.domain.entity.PrivateMessage;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface PrivateMessageMapper extends BaseMapper<PrivateMessage> {
    List<PrivateMessage> selectPrivateMessages(Long conversationId, LocalDateTime after, LocalDateTime before, int limit);

    Integer acknowledgeMessages(List<Long> messageIds);
}
