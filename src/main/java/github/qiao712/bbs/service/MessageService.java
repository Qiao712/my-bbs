package github.qiao712.bbs.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import github.qiao712.bbs.domain.base.PageQuery;
import github.qiao712.bbs.domain.dto.MessageDto;
import github.qiao712.bbs.domain.dto.message.MessageContent;
import github.qiao712.bbs.domain.entity.Message;

import java.util.List;


public interface MessageService extends IService<Message> {

    /**
     * 发送消息
     */
    boolean sendMessage(Long senderId, Long receiverId, String key, MessageContent content);

    /**
     * 删除消息
     * 模糊查找并删除
     */
    boolean removeMessages(Long senderId, Long receiverId, Class<? extends MessageContent> type, List<String> keys);

    /**
     * 获取其他消息的数量
     */
    Long getUnacknowledgedSystemMessageCount();

    /**
     * 获取通知信息(除用户私信外的其他消息)
     */
    IPage<MessageDto> listSystemMessages(PageQuery pageQuery);
}
