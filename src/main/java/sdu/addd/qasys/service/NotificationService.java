package sdu.addd.qasys.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import sdu.addd.qasys.common.PageQuery;
import sdu.addd.qasys.dto.message.NotificationContent;
import sdu.addd.qasys.entity.Notification;

import java.util.List;


public interface NotificationService extends IService<Notification> {

    /**
     * 发送通知
     */
    boolean sendNotification(Long receiverId, String key, NotificationContent content);

    /**
     * 删除通知
     */
    boolean removeNotification(Long receiverId, String notificationType, List<String> keys);

    /**
     * 获取通知信息
     */
    IPage<Notification> listNotifications(PageQuery pageQuery, String notificationType);

    /**
     * 标记当前用户对所有notificationType类型的消息已读
     * @param notificationType 消息类型，为null时表示所有类型
     */
    void acknowledgeNotifications(String notificationType);

    /**
     * 查询当前用户是否对所有notificationType类型的消息已读
     * @param notificationType 消息类型，为null时表示所有类型
     */
    boolean isAcknowledged(String notificationType);
}
