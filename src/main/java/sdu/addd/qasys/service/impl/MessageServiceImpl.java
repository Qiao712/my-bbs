package sdu.addd.qasys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sdu.addd.qasys.common.PageQuery;
import sdu.addd.qasys.common.ResultCode;
import sdu.addd.qasys.dto.ConversationDto;
import sdu.addd.qasys.dto.UserDto;
import sdu.addd.qasys.entity.Conversation;
import sdu.addd.qasys.entity.Message;
import sdu.addd.qasys.entity.User;
import sdu.addd.qasys.exception.ServiceException;
import sdu.addd.qasys.mapper.ConversationMapper;
import sdu.addd.qasys.mapper.MessageMapper;
import sdu.addd.qasys.mapper.UserMapper;
import sdu.addd.qasys.service.MessageService;
import sdu.addd.qasys.service.UserService;
import sdu.addd.qasys.util.PageUtil;
import sdu.addd.qasys.util.SecurityUtil;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message> implements MessageService {
    @Autowired
    private MessageMapper messageMapper;
    @Autowired
    private ConversationMapper conversationMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private UserService userService;

    @Override
    @Transactional
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
            conversation.setUnreadNum1(0);
            conversation.setUnreadNum2(0);
            conversationMapper.insert(conversation);
        }

        Message message = new Message();
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        message.setContent(content);
        message.setConversationId(conversation.getId());
        message.setCreateTime(LocalDateTime.now());
        messageMapper.insert(message);

        //更新会话状态
        conversation.setUser2Id(null);
        conversation.setUser1Id(null);
        conversation.setLastMessageId(message.getId());
        conversation.setLastMessageTime(message.getCreateTime());
        if(receiverId < senderId) conversation.setUnreadNum1(conversation.getUnreadNum1()+1);
        else conversation.setUnreadNum2(conversation.getUnreadNum2()+1);
        conversationMapper.updateById(conversation);
    }

    @Override
    public IPage<ConversationDto> listConversations(PageQuery pageQuery) {
        Long currentUserId = SecurityUtil.getCurrentUser().getId();

        IPage<Conversation> conversationPage = conversationMapper.selectConversations(pageQuery.getIPage(), currentUserId);
        List<Conversation> conversations = conversationPage.getRecords();

        //获取用户信息
        Set<Long> userIds = conversations.stream().map(Conversation::getUser1Id).collect(Collectors.toSet());
        Set<Long> userIds2 = conversations.stream().map(Conversation::getUser2Id).collect(Collectors.toSet());
        userIds.addAll(userIds2);
        Map<Long, UserDto> userMap = userService.listUsers(userIds).stream().collect(Collectors.toMap(UserDto::getId, e -> e));

        //聚合对方用户信息、未读消息数...
        List<ConversationDto> conversationDtos = new ArrayList<>(conversations.size());
        for (Conversation conversation : conversations) {
            ConversationDto conversationDto = new ConversationDto();

            //最后一条消息
            conversationDto.setLatestMessage(conversation.getLastMessage());

            //设置对方用户信息
            Long userId = conversation.getUser1Id().equals(currentUserId) ? conversation.getUser2Id() : conversation.getUser1Id();
            UserDto user = userMap.get(userId);
            conversationDto.setUserId(userId);
            conversationDto.setAvatarUrl(user.getAvatarUrl());
            conversationDto.setUsername(user.getUsername());

            //获取会话内未读消息数量
            if(Objects.equals(currentUserId, conversation.getUser1Id())){
                conversationDto.setUnreadNum(conversation.getUnreadNum1());
            }else{
                conversationDto.setUnreadNum(conversation.getUnreadNum2());
            }

            conversationDtos.add(conversationDto);
        }

        return PageUtil.replaceRecords(conversationPage, conversationDtos);
    }

    @Override
    public List<Message> listMessages(Long senderId, LocalDateTime after, LocalDateTime before, Integer limit) {
        if(senderId == null){
            throw new ServiceException(ResultCode.INVALID_PARAM, "未指定发送者");
        }
        Long currentUserId = SecurityUtil.getCurrentUser().getId();

        if(limit == null){
            limit = 100;
        }

        //获取会话ID
        LambdaQueryWrapper<Conversation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Conversation::getUser1Id, Math.min(currentUserId, senderId));
        queryWrapper.eq(Conversation::getUser2Id, Math.max(currentUserId, senderId));
        Conversation conversation = conversationMapper.selectOne(queryWrapper);
        if(conversation == null){
            return Collections.emptyList();
        }

        //清空未读消息数量
        Conversation update = new Conversation();
        update.setId(conversation.getId());
        if(currentUserId < senderId) update.setUnreadNum1(0);
        else update.setUnreadNum2(0);
        conversationMapper.updateById(update);

        return messageMapper.selectMessages(conversation.getId(), after, before, limit);
    }

    @Override
    @Transactional
    public void acknowledge(Long senderId) {
        Long currentUserId = SecurityUtil.getCurrentUser().getId();
        if(Objects.equals(senderId, currentUserId)){
            throw new ServiceException(ResultCode.INVALID_PARAM);
        }

        if(senderId != null){
            //清除未读的senderId发送的消息计数
            LambdaUpdateWrapper<Conversation> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(Conversation::getUser1Id, Math.min(senderId, currentUserId));
            updateWrapper.eq(Conversation::getUser2Id, Math.max(senderId, currentUserId));
            updateWrapper.set(currentUserId < senderId, Conversation::getUnreadNum1, 0);    //当前用户的ID小，其为user1
            updateWrapper.set(currentUserId > senderId, Conversation::getUnreadNum2, 0);    //当前用户的ID大，其为user2
            conversationMapper.update(null, updateWrapper);
        }else{
            //确认全部
            LambdaUpdateWrapper<Conversation> updateWrapper1 = new LambdaUpdateWrapper<>();
            updateWrapper1.eq(Conversation::getUser1Id, currentUserId);
            updateWrapper1.set(Conversation::getUnreadNum1, 0);
            conversationMapper.update(null, updateWrapper1);

            LambdaUpdateWrapper<Conversation> updateWrapper2 = new LambdaUpdateWrapper<>();
            updateWrapper2.eq(Conversation::getUser2Id, currentUserId);
            updateWrapper2.set(Conversation::getUnreadNum2, 0);
            conversationMapper.update(null, updateWrapper2);
        }

    }

    @Override
    public Long getUnreadNumber() {
        Long currentUserId = SecurityUtil.getCurrentUser().getId();
        return conversationMapper.selectUnreadNumber(currentUserId);
    }
}
