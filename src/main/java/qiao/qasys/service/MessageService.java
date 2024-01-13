package qiao.qasys.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import qiao.qasys.common.PageQuery;
import qiao.qasys.dto.ConversationDto;
import qiao.qasys.entity.Message;

import java.time.LocalDateTime;
import java.util.List;

public interface MessageService extends IService<Message> {
    /**
     * 发送消息
     */
    void sendMessage(Long receiverId, String content);

    /**
     * 列出会话列表
     */
    IPage<ConversationDto> listConversations(PageQuery pageQuery);

    /**
     * 根据时间范围，获取与某用户会话中的消息列表
     * @param receiverId 对话用户
     * @param after 在此时刻之后的消息，为空则不限制
     * @param before 在时时刻之前的消息，为空则不限制
     * @param limit 获取数量
     */
    List<Message> listMessages(Long receiverId, LocalDateTime after, LocalDateTime before, Integer limit);

    /**
     * 确认与某用户对话消息
     * 若userId为空，则确认全部消息
     */
    void acknowledge(Long senderId);

    /**
     * 获取当前用户未读的私信消息数量
     */
    Long getUnreadNumber();
}
