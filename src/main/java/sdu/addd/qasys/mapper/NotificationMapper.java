package sdu.addd.qasys.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import sdu.addd.qasys.entity.Notification;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface NotificationMapper extends BaseMapper<Notification> {

}
