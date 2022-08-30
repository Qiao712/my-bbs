package github.qiao712.bbs.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import github.qiao712.bbs.domain.base.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@TableName("t_file")
@ApiModel(value = "文件标识对象", description = "文件记录")
@Data
@EqualsAndHashCode(callSuper = true)
public class FileIdentity extends BaseEntity {
    @ApiModelProperty("文件路径")
    private String filepath;

    @ApiModelProperty("文件类型")
    private String type;

    @ApiModelProperty("指向上传者")
    private Long uploaderId;

    @ApiModelProperty("是否为临时文件")
    private Boolean isTemporary;

    @ApiModelProperty("文件来源(分类)(用于标识用途)")
    private String source;
}
