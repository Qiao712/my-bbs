package github.qiao712.bbs.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import github.qiao712.bbs.domain.base.PageQuery;
import github.qiao712.bbs.domain.dto.ConversationDto;
import github.qiao712.bbs.domain.dto.MessageDto;
import github.qiao712.bbs.domain.dto.message.MessageContent;
import github.qiao712.bbs.domain.dto.message.PrivateMessageContent;
import github.qiao712.bbs.domain.entity.Message;
import github.qiao712.bbs.domain.entity.User;
import github.qiao712.bbs.exception.ServiceException;
import github.qiao712.bbs.mapper.MessageMapper;
import github.qiao712.bbs.mapper.UserMapper;
import github.qiao712.bbs.service.MessageService;
import github.qiao712.bbs.service.UserService;
import github.qiao712.bbs.util.PageUtil;
import github.qiao712.bbs.util.SecurityUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message> implements MessageService {
    @Autowired
    private MessageMapper messageMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private UserService userService;

    @Override
    public boolean sendMessage(Long senderId, Long receiverId, MessageContent content) {
        Message message = new Message();
        message.setContent(JSON.toJSONString(content));
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        message.setType(getMessageType(content.getClass()));
        message.setIsAcknowledged(false);

        //两个用户之间的会话id
        if(senderId != null && receiverId != null){
            message.setConversationId(getConversationId(senderId, receiverId));
        }

        return messageMapper.insert(message) > 0;
    }

    @Override
    public boolean sendPrivateMessage(Long receiverId, String text) {
        Long senderId = SecurityUtil.getCurrentUser().getId();

        if(Objects.equals(receiverId, senderId)){
            throw new ServiceException("禁止向自己发送私信");
        }

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", receiverId);
        if(!userMapper.exists(queryWrapper)){
            throw new ServiceException("目标用户不存在");
        }

        PrivateMessageContent messageContent = new PrivateMessageContent();
        messageContent.setText(text);
        return sendMessage(senderId, receiverId, messageContent);
    }

    @Override
    public IPage<ConversationDto> listConversations(PageQuery pageQuery) {
        Long currentUserId = SecurityUtil.getCurrentUser().getId();

        IPage<Byte[]> conversationIdPage = messageMapper.selectConversationIds(pageQuery.getIPage(), currentUserId);
        List<Byte[]> conversationIds = conversationIdPage.getRecords();

        List<ConversationDto> conversationDtos = new ArrayList<>(conversationIds.size());
        for (Byte[] conversationId : conversationIds) {
            Message message = messageMapper.selectLatestMessageByConversationId(conversationId);
            ConversationDto conversationDto = new ConversationDto();

            conversationDto.setCreateTime(message.getCreateTime());
            conversationDto.setIsAcknowledged(message.getIsAcknowledged());

            PrivateMessageContent messageContent = JSON.parseObject(message.getContent(), PrivateMessageContent.class);
            conversationDto.setLatestMessage(messageContent.getText());

            //设置对方用户信息
            Long userId = !Objects.equals(message.getReceiverId(), currentUserId) ? message.getReceiverId() : message.getSenderId();
            User user = userService.getUser(userId);
            conversationDto.setUserId(userId);
            conversationDto.setAvatarUrl(user.getAvatarUrl());
            conversationDto.setUsername(user.getUsername());

            conversationDtos.add(conversationDto);
        }

        return PageUtil.replaceRecords(conversationIdPage, conversationDtos);
    }

    @Override
    public List<MessageDto> listPrivateMessages(Long receiverId, LocalDateTime after, LocalDateTime before, Integer limit) {
        Long currentUserId = SecurityUtil.getCurrentUser().getId();

        if(limit == null){
            limit = 100;
        }

        List<Message> messages = messageMapper.selectPrivateMessages(currentUserId, receiverId, after, before, limit);

        List<MessageDto> messageDtos = new ArrayList<>(messages.size());
        for (Message message : messages) {
            messageDtos.add(convertToMessageDto(message, PrivateMessageContent.class));
        }

        return messageDtos;
    }

    @Override
    public Long getUnacknowledgedPrivateMessageCount() {
        Long currentUserId = SecurityUtil.getCurrentUser().getId();

        Message messageQuery = new Message();
        messageQuery.setReceiverId(currentUserId);
        messageQuery.setType("private");
        messageQuery.setIsAcknowledged(false);
        return messageMapper.selectCount(new QueryWrapper<>(messageQuery));
    }

    @Override
    public Long getUnacknowledgedMessageCount() {
        Long currentUserId = SecurityUtil.getCurrentUser().getId();

        LambdaQueryWrapper<Message> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Message::getReceiverId, currentUserId);
        queryWrapper.ne(Message::getType, "private");
        queryWrapper.eq(Message::getIsAcknowledged, false);
        return messageMapper.selectCount(queryWrapper);
    }

    @Override
    public IPage<MessageDto> listMessages(PageQuery pageQuery) {

        return null;
    }

    /**
     * 根据MessageContent类型获取消息类型（xxxMessageContent --> xxx）
     */
    private String getMessageType(Class<? extends MessageContent> cls){
        String simpleName = cls.getSimpleName();
        int i = simpleName.indexOf("MessageContent");
        if(i == -1){
            throw new IllegalArgumentException("MessageContent子类命名不规范,应以MessageContent为后缀.");
        }
        return simpleName.substring(0, i);
    }

    /**
     * 计算会话id
     * 将senderId和receiverId的字节序列拼接，较大的放在高8位，较小的放在低8位。
     */
    private byte[] getConversationId(long senderId, long receiverId){
        long bigger = Math.max(senderId, receiverId);
        long smaller = Math.min(senderId, receiverId);
        byte[] conversationId = new byte[16];
        long mask = 0xFF;

        for(int i = 0; i < 8; i++){
            conversationId[i] = (byte) (smaller & mask);
            conversationId[i+8] = (byte) (bigger & mask);
            smaller >>= 8;
            bigger >>= 8;
        }

        return conversationId;
    }

    private MessageDto convertToMessageDto(Message message, Class<? extends MessageContent> cls){
        MessageDto messageDto = new MessageDto();
        BeanUtils.copyProperties(message, messageDto);
        messageDto.setMessageContent(JSON.parseObject(message.getContent(), cls));
        return messageDto;
    }
}
