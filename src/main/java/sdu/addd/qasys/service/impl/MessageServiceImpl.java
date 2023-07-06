package sdu.addd.qasys.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import sdu.addd.qasys.dto.MessageDto;
import sdu.addd.qasys.dto.message.MessageContent;
import sdu.addd.qasys.service.MessageService;
import sdu.addd.qasys.common.PageQuery;
import sdu.addd.qasys.dto.message.MessageType;
import sdu.addd.qasys.entity.Message;
import sdu.addd.qasys.mapper.MessageMapper;
import sdu.addd.qasys.util.PageUtil;
import sdu.addd.qasys.util.SecurityUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message> implements MessageService {
    @Autowired
    private MessageMapper messageMapper;

    @Override
    public boolean sendMessage(Long senderId, Long receiverId, String key, MessageContent content) {
        Message message = new Message();
        message.setContent(JSON.toJSONString(content));
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        message.setMessageKey(key);
        message.setType(getMessageType(content.getClass()));
        message.setIsAcknowledged(false);

        return messageMapper.insert(message) > 0;
    }

    @Override
    public boolean removeMessages(Long senderId, Long receiverId, Class<? extends MessageContent> type, List<String> keys) {
        LambdaQueryWrapper<Message> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(receiverId!=null,Message::getReceiverId, receiverId)
                    .eq(senderId!=null, Message::getSenderId, senderId)
                    .eq(type!=null, Message::getType, getMessageType(type))
                    .in(keys != null && !keys.isEmpty(), Message::getMessageKey, keys);
        return messageMapper.delete(queryWrapper) > 0;
    }


    @Override
    public Long getUnacknowledgedSystemMessageCount() {
        Long currentUserId = SecurityUtil.getCurrentUser().getId();

        LambdaQueryWrapper<Message> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Message::getReceiverId, currentUserId);
        queryWrapper.ne(Message::getType, "private");
        queryWrapper.eq(Message::getIsAcknowledged, false);
        return messageMapper.selectCount(queryWrapper);
    }

    @Override
    public IPage<MessageDto> listSystemMessages(PageQuery pageQuery) {
        Long currentUserId = SecurityUtil.getCurrentUser().getId();

        LambdaQueryWrapper<Message> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Message::getReceiverId, currentUserId);
        queryWrapper.ne(Message::getType, "private");
        queryWrapper.orderByDesc(Message::getCreateTime);
        IPage<Message> messagePage = messageMapper.selectPage(pageQuery.getIPage(), queryWrapper);
        List<Message> messages = messagePage.getRecords();

        //确认消息
        List<Long> messageIds = new ArrayList<>(messages.size());
        for (Message message : messages) {
            if(Objects.equals(message.getReceiverId(), currentUserId) && !message.getIsAcknowledged()){
                messageIds.add(message.getId());
            }
        }
        if(!messageIds.isEmpty()) messageMapper.acknowledgeMessages(messageIds);

        List<MessageDto> messageDtos = messages.stream().map(this::convertToMessageDto).collect(Collectors.toList());
        return PageUtil.replaceRecords(messagePage, messageDtos);
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
     * 根据消息类型，反序列化content
     */
    private MessageDto convertToMessageDto(Message message){
        String typeName = message.getType();
        return convertToMessageDto(message, MessageType.getMessageContentTypeClass(typeName));
    }

    private MessageDto convertToMessageDto(Message message, Class<? extends MessageContent> cls){
        MessageDto messageDto = new MessageDto();
        BeanUtils.copyProperties(message, messageDto);
        messageDto.setContent(JSON.parseObject(message.getContent(), cls));
        return messageDto;
    }
}
