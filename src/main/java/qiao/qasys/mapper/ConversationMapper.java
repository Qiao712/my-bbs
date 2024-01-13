package qiao.qasys.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import qiao.qasys.entity.Conversation;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ConversationMapper extends BaseMapper<Conversation> {
    IPage<Conversation> selectConversations(IPage<Conversation> page, Long userId);

    Long selectUnreadNumber(Long receiverId);
}
