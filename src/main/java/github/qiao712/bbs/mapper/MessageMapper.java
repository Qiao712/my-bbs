package github.qiao712.bbs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import github.qiao712.bbs.domain.entity.Message;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface MessageMapper extends BaseMapper<Message> {
    IPage<Byte[]> selectConversationIds(IPage<?> page, Long userId);

    Message selectLatestMessageByConversationId(Byte[] conversationId);

    List<Message> selectPrivateMessages(byte[] conversationId, LocalDateTime after, LocalDateTime before, int limit);

    Integer acknowledgeMessages(List<Long> messageIds);
}
