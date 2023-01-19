package github.qiao712.bbs.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import github.qiao712.bbs.domain.base.PageQuery;
import github.qiao712.bbs.domain.base.ResultCode;
import github.qiao712.bbs.domain.dto.AuthUser;
import github.qiao712.bbs.domain.dto.ConversationDto;
import github.qiao712.bbs.domain.dto.PrivateMessageDto;
import github.qiao712.bbs.domain.entity.Conversation;
import github.qiao712.bbs.domain.entity.PrivateMessage;
import github.qiao712.bbs.domain.entity.User;
import github.qiao712.bbs.exception.ServiceException;
import github.qiao712.bbs.mapper.ConversationMapper;
import github.qiao712.bbs.mapper.PrivateMessageMapper;
import github.qiao712.bbs.mapper.UserMapper;
import github.qiao712.bbs.service.ChatService;
import github.qiao712.bbs.service.UserService;
import github.qiao712.bbs.util.PageUtil;
import github.qiao712.bbs.util.SecurityUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Service
public class ChatServiceImpl extends ServiceImpl<PrivateMessageMapper, PrivateMessage> implements ChatService {
    @Autowired
    private PrivateMessageMapper privateMessageMapper;
    @Autowired
    private ConversationMapper conversationMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private UserService userService;

    @Override
    public void sendMessage(Long receiverId, String content) {
        Long senderId = SecurityUtil.getCurrentUser().getId();

        //检查参数
        if(content.length() == 0 || content.length() > 500){
            throw new ServiceException(ResultCode.MESSAGE_ERROR, "消息长度不合法");
        }
        if(receiverId == null) {
            throw new ServiceException(ResultCode.MESSAGE_ERROR, "接收者不可为空");
        }
        if(Objects.equals(receiverId, senderId)){
            throw new ServiceException(ResultCode.MESSAGE_ERROR, "禁止向自己发送私信");
        }
        if(!userMapper.exists(new LambdaQueryWrapper<User>().eq(User::getId, receiverId))){
            throw new ServiceException(ResultCode.MESSAGE_ERROR, "目标用户不存在");
        }

        //获取会话ID
        LambdaQueryWrapper<Conversation> conversationQueryWrapper = new LambdaQueryWrapper<>();
        conversationQueryWrapper.eq(Conversation::getUser1Id, Math.min(senderId, receiverId));
        conversationQueryWrapper.eq(Conversation::getUser2Id, Math.max(senderId, receiverId));
        Conversation conversation = conversationMapper.selectOne(conversationQueryWrapper);
        if(conversation == null){
            //不存在则创建
            conversation = new Conversation();
            conversation.setUser1Id(Math.min(senderId, receiverId));
            conversation.setUser2Id(Math.max(senderId, receiverId));
            conversationMapper.insert(conversation);
        }

        PrivateMessage privateMessage = new PrivateMessage();
        privateMessage.setSenderId(senderId);
        privateMessage.setReceiverId(receiverId);
        privateMessage.setContent(content);
        privateMessage.setType(1);
        privateMessage.setConversationId(conversation.getId());
        privateMessageMapper.insert(privateMessage);

        //更新会话状态
        conversation.setLastMessageId(privateMessage.getId());
        conversation.setLastMessageTime(privateMessage.getCreateTime());
        conversationMapper.updateById(conversation);
    }

    @Override
    public IPage<ConversationDto> listConversations(PageQuery pageQuery) {
        Long currentUserId = SecurityUtil.getCurrentUser().getId();

        IPage<Conversation> conversationPage = conversationMapper.selectConversations(pageQuery.getIPage(), currentUserId);
        List<Conversation> conversations = conversationPage.getRecords();

        //聚合对方用户信息、未读消息数...
        List<ConversationDto> conversationDtos = new ArrayList<>(conversations.size());
        for (Conversation conversation : conversations) {
            ConversationDto conversationDto = new ConversationDto();

            //设置对方用户信息
            Long userId = conversation.getUser1Id().equals(currentUserId) ? conversation.getUser2Id() : conversation.getUser1Id();
            User user = userService.getUser(userId);
            conversationDto.setUserId(userId);
            conversationDto.setAvatarUrl(user.getAvatarUrl());
            conversationDto.setUsername(user.getUsername());

            //获取会话内未读消息数量
            LambdaQueryWrapper<PrivateMessage> privateMessageQueryWrapper = new LambdaQueryWrapper<>();
            privateMessageQueryWrapper.eq(PrivateMessage::getReceiverId, currentUserId);
            privateMessageQueryWrapper.eq(PrivateMessage::getIsAcknowledged, false);
            conversationDto.setUnacknowledgedCount(privateMessageMapper.selectCount(privateMessageQueryWrapper));

            //最后一条消息
            conversationDto.setLatestMessage(convertToPrivateMessageDto(conversation.getLastMessage()));
            conversationDtos.add(conversationDto);
        }

        return PageUtil.replaceRecords(conversationPage, conversationDtos);
    }

    @Override
    public List<PrivateMessageDto> listPrivateMessages(Long receiverId, LocalDateTime after, LocalDateTime before, Integer limit) {
        Long currentUserId = SecurityUtil.getCurrentUser().getId();

        if(limit == null){
            limit = 100;
        }

        //获取会话ID
        LambdaQueryWrapper<Conversation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Conversation::getUser1Id, Math.min(currentUserId, receiverId));
        queryWrapper.eq(Conversation::getUser2Id, Math.max(currentUserId, receiverId));
        Conversation conversation = conversationMapper.selectOne(queryWrapper);
        if(conversation == null){
            return Collections.emptyList();
        }

        List<PrivateMessage> messages = privateMessageMapper.selectPrivateMessages(conversation.getId(), after, before, limit);
        List<PrivateMessageDto> messageDtos = new ArrayList<>(messages.size());
        for (PrivateMessage message : messages) {
            messageDtos.add(convertToPrivateMessageDto(message));
        }

        return messageDtos;
    }

    @Override
    public boolean acknowledge(Long userId) {
        AuthUser currentUser = SecurityUtil.getCurrentUser();

        LambdaUpdateWrapper<PrivateMessage> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(PrivateMessage::getReceiverId, currentUser.getId());
        if(userId != null){
            updateWrapper.eq(PrivateMessage::getSenderId, userId);
        }
        updateWrapper.set(PrivateMessage::getIsAcknowledged, true);

        return privateMessageMapper.update(null, updateWrapper) > 0;
    }

    @Override
    public Long getUnacknowledgedPrivateMessageCount() {
        Long currentUserId = SecurityUtil.getCurrentUser().getId();

        PrivateMessage messageQuery = new PrivateMessage();
        messageQuery.setReceiverId(currentUserId);
        messageQuery.setIsAcknowledged(false);
        return privateMessageMapper.selectCount(new QueryWrapper<>(messageQuery));
    }

    private PrivateMessageDto convertToPrivateMessageDto(PrivateMessage privateMessage){
        PrivateMessageDto privateMessageDto = new PrivateMessageDto();
        BeanUtils.copyProperties(privateMessage, privateMessageDto);
        return privateMessageDto;
    }
}
