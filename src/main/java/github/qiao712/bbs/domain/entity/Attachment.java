package github.qiao712.bbs.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import github.qiao712.bbs.domain.base.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@TableName("t_attachment")
@ApiModel(value = "附件对象", description = "记录贴子内容或其评论中的图片等附加文件")
@Data
@EqualsAndHashCode(callSuper = true)
public class Attachment extends BaseEntity {
    @ApiModelProperty("指向贴子")
    private Long postId;

    @ApiModelProperty("指向该贴子中的评论(若为null)，则表示指向贴子内容")
    private Long commentId;

    @ApiModelProperty("指向属于于该贴子的文件记录")
    private Long fileId;
}
