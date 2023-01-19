package github.qiao712.bbs.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import github.qiao712.bbs.domain.base.PageQuery;
import github.qiao712.bbs.domain.dto.ConversationDto;
import github.qiao712.bbs.domain.dto.PrivateMessageDto;
import github.qiao712.bbs.domain.entity.PrivateMessage;

import java.time.LocalDateTime;
import java.util.List;

public interface ChatService extends IService<PrivateMessage> {
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
    List<PrivateMessageDto> listPrivateMessages(Long receiverId, LocalDateTime after, LocalDateTime before, Integer limit);

    /**
     * 确认与某用户对话消息
     * 若会话ID为空，则确认全部消息
     */
    boolean acknowledge(Long userId);

    /**
     * 获取未读的私信消息数量
     */
    Long getUnacknowledgedPrivateMessageCount();
}
