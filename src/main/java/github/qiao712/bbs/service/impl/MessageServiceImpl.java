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
import github.qiao712.bbs.domain.dto.message.MessageType;
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
import java.util.stream.Collectors;

@Service
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message> implements MessageService {
    @Autowired
    private MessageMapper messageMapper;

    @Override
    public boolean sendMessage(Long senderId, Long receiverId, MessageContent content) {
        Message message = new Message();
        message.setContent(JSON.toJSONString(content));
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        message.setType(getMessageType(content.getClass()));
        message.setIsAcknowledged(false);

        return messageMapper.insert(message) > 0;
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

        //????????????
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
     * ??????MessageContent???????????????????????????xxxMessageContent --> xxx???
     */
    private String getMessageType(Class<? extends MessageContent> cls){
        String simpleName = cls.getSimpleName();
        int i = simpleName.indexOf("MessageContent");
        if(i == -1){
            throw new IllegalArgumentException("MessageContent?????????????????????,??????MessageContent?????????.");
        }
        return simpleName.substring(0, i);
    }

    /**
     * ?????????????????????????????????content
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
