package github.qiao712.bbs.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import github.qiao712.bbs.domain.base.PageQuery;
import github.qiao712.bbs.domain.dto.ConversationDto;
import github.qiao712.bbs.domain.dto.MessageDto;
import github.qiao712.bbs.domain.dto.PrivateMessageDto;
import github.qiao712.bbs.domain.dto.message.PrivateMessageContent;
import github.qiao712.bbs.domain.entity.Message;
import github.qiao712.bbs.domain.entity.PrivateMessage;
import github.qiao712.bbs.domain.entity.User;
import github.qiao712.bbs.exception.ServiceException;
import github.qiao712.bbs.mapper.PrivateMessageMapper;
import github.qiao712.bbs.mapper.UserMapper;
import github.qiao712.bbs.service.ChatService;
import github.qiao712.bbs.service.UserService;
import github.qiao712.bbs.util.PageUtil;
import github.qiao712.bbs.util.SecurityUtil;
import github.qiao712.bbs.websocket.ChatChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
@Slf4j
public class ChatServiceImpl extends ServiceImpl<PrivateMessageMapper, PrivateMessage> implements ChatService {
    //用户ID --> websocket的session
    private final ConcurrentMap<Long, ChatChannel> channels = new ConcurrentHashMap<>();

    @Autowired
    private PrivateMessageMapper privateMessageMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private UserService userService;

    @Override
    public void addChannel(ChatChannel channel){
        ChatChannel oldChannel = channels.putIfAbsent(channel.getUserId(), channel);

        //将旧连接下线
        if(oldChannel != null){
            try {
                oldChannel.close();
            } catch (IOException e) {
                log.error("Websocket Close:", e);
            }
        }
    }

    @Override
    public void removeChannel(ChatChannel channel){
        channels.remove(channel.getUserId());
    }

    @Override
    public void handlerMessages(ChatChannel channel, String message) {
        PrivateMessageDto privateMessageDto = JSON.parseObject(message, PrivateMessageDto.class);
        Long receiverId = privateMessageDto.getReceiverId();
        Long senderId = channel.getUserId();

        //检查参数
        if(privateMessageDto.getContent().length() <= 0 || privateMessageDto.getContent().length() > 500){
            throw new ServiceException("消息长度不合法");
        }
        if(privateMessageDto.getType() != 1){
            throw new ServiceException("消息类型非法");
        }
        if(receiverId == null) {
             throw new ServiceException("接收者不可为空");
        }
        if(Objects.equals(receiverId, senderId)){
            throw new ServiceException("禁止向自己发送私信");
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", receiverId);
        if(!userMapper.exists(queryWrapper)){
            throw new ServiceException("目标用户不存在");
        }

        //持久化消息
        PrivateMessage privateMessage = new PrivateMessage();
        privateMessage.setSenderId(senderId);
        privateMessage.setReceiverId(receiverId);
        privateMessage.setContent(privateMessageDto.getContent());
        privateMessage.setType(privateMessage.getType());
        privateMessage.setConversationId(getConversationId(senderId, receiverId));
        privateMessageMapper.insert(privateMessage);

        //转发给接收者
        ChatChannel receiverChannel = channels.get(receiverId);
        if(receiverChannel != null){
            privateMessageDto.setSenderId(senderId);
            receiverChannel.send(privateMessageDto);
        }
    }

    @Override
    public IPage<ConversationDto> listConversations(PageQuery pageQuery) {
        Long currentUserId = SecurityUtil.getCurrentUser().getId();

        //获取会话ID列表
        IPage<Byte[]> conversationIdPage = privateMessageMapper.selectConversationIds(pageQuery.getIPage(), currentUserId);
        List<Byte[]> conversationIds = conversationIdPage.getRecords();

        //聚合最新一条消息，对方用户信息等信息
        List<ConversationDto> conversationDtos = new ArrayList<>(conversationIds.size());
        for (Byte[] conversationId : conversationIds) {
            PrivateMessage message = privateMessageMapper.selectLatestMessageByConversationId(conversationId);
            ConversationDto conversationDto = new ConversationDto();

            conversationDto.setCreateTime(message.getCreateTime());

            PrivateMessageContent messageContent = JSON.parseObject(message.getContent(), PrivateMessageContent.class);
            conversationDto.setLatestMessage(messageContent.getText());

            //设置对方用户信息
            Long userId = !Objects.equals(message.getReceiverId(), currentUserId) ? message.getReceiverId() : message.getSenderId();
            User user = userService.getUser(userId);
            conversationDto.setUserId(userId);
            conversationDto.setAvatarUrl(user.getAvatarUrl());
            conversationDto.setUsername(user.getUsername());

            //获取会话内未读消息数量
            LambdaQueryWrapper<PrivateMessage> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(PrivateMessage::getIsAcknowledged, false);
            queryWrapper.eq(PrivateMessage::getConversationId, conversationId);
            queryWrapper.eq(PrivateMessage::getReceiverId, currentUserId);
            conversationDto.setUnacknowledgedCount(privateMessageMapper.selectCount(queryWrapper));

            conversationDtos.add(conversationDto);
        }

        return PageUtil.replaceRecords(conversationIdPage, conversationDtos);
    }

    @Override
    public List<PrivateMessageDto> listPrivateMessages(Long receiverId, LocalDateTime after, LocalDateTime before, Integer limit) {
        Long currentUserId = SecurityUtil.getCurrentUser().getId();

        if(limit == null){
            limit = 100;
        }

        byte[] conversationId = getConversationId(currentUserId, receiverId);
        List<PrivateMessage> messages = privateMessageMapper.selectPrivateMessages(conversationId, after, before, limit);

        List<PrivateMessageDto> messageDtos = new ArrayList<>(messages.size());
        for (PrivateMessage message : messages) {
            messageDtos.add(convertToPrivateMessageDto(message));
        }

        //确认消息
        List<Long> messageIds = new ArrayList<>(messages.size());
        for (PrivateMessage message : messages) {
            if(Objects.equals(message.getReceiverId(), currentUserId) && !message.getIsAcknowledged()){
                messageIds.add(message.getId());
            }
        }
        if(!messageIds.isEmpty()) privateMessageMapper.acknowledgeMessages(messageIds);

        return messageDtos;
    }

    @Override
    public Long getUnacknowledgedPrivateMessageCount() {
        Long currentUserId = SecurityUtil.getCurrentUser().getId();

        PrivateMessage messageQuery = new PrivateMessage();
        messageQuery.setReceiverId(currentUserId);
        messageQuery.setIsAcknowledged(false);
        return privateMessageMapper.selectCount(new QueryWrapper<>(messageQuery));
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

    private PrivateMessageDto convertToPrivateMessageDto(PrivateMessage privateMessage){
        PrivateMessageDto privateMessageDto = new PrivateMessageDto();
        BeanUtils.copyProperties(privateMessage, privateMessageDto);
        return privateMessageDto;
    }
}
