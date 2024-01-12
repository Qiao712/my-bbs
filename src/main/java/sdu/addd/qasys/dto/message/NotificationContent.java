package sdu.addd.qasys.dto.message;

import com.alibaba.fastjson.annotation.JSONField;

public abstract class NotificationContent {
    @JSONField(serialize = false)
    private final String NOTIFICATION_TYPE;

    protected NotificationContent(String notificationType) {
        this.NOTIFICATION_TYPE = notificationType;
    }

    public String getNotificationType(){
        return NOTIFICATION_TYPE;
    }
}
