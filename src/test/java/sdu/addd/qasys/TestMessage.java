package sdu.addd.qasys;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import sdu.addd.qasys.mapper.NotificationStateMapper;

@SpringBootTest
public class TestMessage {
    @Autowired
    private NotificationStateMapper notificationStateMapper;

    @Test
    public void testNotificationState(){
        notificationStateMapper.insertOrUpdate(1L, "like", false);
        notificationStateMapper.insertOrUpdate(1L, "answer", false);
        notificationStateMapper.insertOrUpdate(1L, "invite", false);
        notificationStateMapper.insertOrUpdate(1L, "invite", false);
    }
}
