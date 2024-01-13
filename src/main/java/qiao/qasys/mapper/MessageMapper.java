package qiao.qasys.mapper;

import qiao.qasys.entity.Message;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface MessageMapper extends BaseMapper<Message> {
    List<Message> selectMessages(Long conversationId, LocalDateTime after, LocalDateTime before, int limit);
}
