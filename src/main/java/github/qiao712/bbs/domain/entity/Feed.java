package github.qiao712.bbs.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.io.Serializable;
import java.sql.Timestamp;

@Data
@TableName("t_feed")
@ApiModel(value = "Feed对象", description = "动态")
public class Feed implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long followeeId;

    private Long followerId;

    private Long postId;

    private Timestamp time;
}