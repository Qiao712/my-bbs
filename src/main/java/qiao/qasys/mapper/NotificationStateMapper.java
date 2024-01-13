package qiao.qasys.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;
import qiao.qasys.entity.NotificationState;

@Mapper
public interface NotificationStateMapper extends BaseMapper<NotificationState> {
    @Update(
            "REPLACE INTO t_notification_state(user_id, notification_type, acknowledged)" +
            "VALUES(#{userId}, #{notificationType}, #{acknowledged})"
    )
    boolean insertOrUpdate(long userId, String notificationType, boolean acknowledged);
}
