package github.qiao712.bbs.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import github.qiao712.bbs.domain.base.PageQuery;
import github.qiao712.bbs.domain.dto.ConversationDto;
import github.qiao712.bbs.domain.dto.MessageDto;
import github.qiao712.bbs.domain.dto.message.MessageContent;
import github.qiao712.bbs.domain.entity.Message;

import java.time.LocalDateTime;
import java.util.List;


public interface MessageService extends IService<Message> {

    /**
     * 发送消息
     */
    boolean sendMessage(Long senderId, Long receiverId, MessageContent content);

    /**
     * 发送私信
     */
    boolean sendPrivateMessage(Long receiverId, String text);

    /**
     * 获取用户与其他用户的会话列表
     * @return 与某用户的会话中的最后一条消息
     */
    IPage<ConversationDto> listConversations(PageQuery pageQuery);

    /**
     * 获取与某用户会话中的消息列表
     * @param receiverId 对话用户
     * @param after 在此时刻之后的消息，为空则不限制
     * @param before 在时时刻之前的消息，为空则不限制
     * @param limit 获取数量
     */
    List<MessageDto> listPrivateMessages(Long receiverId, LocalDateTime after, LocalDateTime before, Integer limit);

    /**
     * 获取未读的私信消息数量
     */
    Long getUnacknowledgedPrivateMessageCount();

    /**
     * 获取其他消息的数量
     */
    Long getUnacknowledgedSystemMessageCount();

    /**
     * 获取通知信息(除用户私信外的其他消息)
     */
    IPage<MessageDto> listSystemMessages(PageQuery pageQuery);
}
