package sdu.addd.qasys.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sdu.addd.qasys.common.PageQuery;
import sdu.addd.qasys.dto.message.NotificationContent;
import sdu.addd.qasys.entity.Notification;
import sdu.addd.qasys.entity.NotificationState;
import sdu.addd.qasys.mapper.NotificationMapper;
import sdu.addd.qasys.mapper.NotificationStateMapper;
import sdu.addd.qasys.service.NotificationService;
import sdu.addd.qasys.util.SecurityUtil;

import java.util.List;

@Service
public class NotificationServiceImpl extends ServiceImpl<NotificationMapper, Notification> implements NotificationService {
    @Autowired
    private NotificationMapper notificationMapper;
    @Autowired
    private NotificationStateMapper notificationStateMapper;

    @Override
    @Transactional
    public boolean sendNotification(Long receiverId, String key, NotificationContent content) {
        Notification notification = new Notification();
        notification.setContent(JSON.toJSONString(content));
        notification.setReceiverId(receiverId);
        notification.setNotificationKey(key);
        notification.setType(content.getNotificationType());

        //消息状态改为未读
        notificationStateMapper.insertOrUpdate(receiverId, content.getNotificationType(), false);

        return notificationMapper.insert(notification) > 0;
    }

    @Override
    public boolean removeNotification(Long receiverId, String notificationType, List<String> keys) {
        LambdaQueryWrapper<Notification> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(receiverId!=null, Notification::getReceiverId, receiverId)
                    .eq(notificationType!=null, Notification::getType, notificationType)
                    .in(keys != null && !keys.isEmpty(), Notification::getNotificationKey, keys);
        return notificationMapper.delete(queryWrapper) > 0;
    }

    @Override
    public IPage<Notification> listNotifications(PageQuery pageQuery, String notificationType) {
        Long currentUserId = SecurityUtil.getCurrentUser().getId();

        LambdaQueryWrapper<Notification> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Notification::getReceiverId, currentUserId);
        queryWrapper.eq(notificationType != null, Notification::getType, notificationType);
        queryWrapper.orderByDesc(Notification::getCreateTime);

        //确认该类型的消息
        acknowledgeNotifications(notificationType);

        return notificationMapper.selectPage(pageQuery.getIPage(), queryWrapper);
    }

    @Override
    public void acknowledgeNotifications(String notificationType){
        Long currentUserId = SecurityUtil.getCurrentUser().getId();

        LambdaQueryWrapper<NotificationState> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(NotificationState::getUserId, currentUserId);
        queryWrapper.eq(notificationType != null, NotificationState::getNotificationType, notificationType);

        NotificationState notificationState = new NotificationState();
        notificationState.setUserId(currentUserId);
        notificationState.setAcknowledged(true);
        notificationStateMapper.update(notificationState, queryWrapper);
    }

    @Override
    public boolean isAcknowledged(String notificationType){
        Long currentUserId = SecurityUtil.getCurrentUser().getId();

        LambdaQueryWrapper<NotificationState> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(NotificationState::getUserId, currentUserId);
        queryWrapper.eq(notificationType != null, NotificationState::getNotificationType, notificationType);
        List<NotificationState> notificationStates = notificationStateMapper.selectList(queryWrapper);

        for (NotificationState notificationState : notificationStates) {
            if(!notificationState.getAcknowledged()) return false;
        }

        return true;
    }
}
