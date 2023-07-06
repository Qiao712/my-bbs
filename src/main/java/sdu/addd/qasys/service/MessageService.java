package sdu.addd.qasys.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import sdu.addd.qasys.common.PageQuery;
import sdu.addd.qasys.dto.MessageDto;
import sdu.addd.qasys.dto.message.MessageContent;
import sdu.addd.qasys.entity.Message;

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
