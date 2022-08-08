package github.qiao712.bbs.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;

import github.qiao712.bbs.domain.base.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@TableName("t_comment")
@ApiModel(value = "Comment对象", description = "评论")
public class Comment extends BaseEntity {

    private String content;

    private Long authorId;

    @ApiModelProperty("所属的一级评论")
    private Long parentId;

    @ApiModelProperty("被回复的评论")
    private Long repliedId;

    @ApiModelProperty("所属贴子id")
    private Long postId;

    @ApiModelProperty("点赞数")
    private Integer likeCount;
}
