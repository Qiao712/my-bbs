package github.qiao712.bbs.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import github.qiao712.bbs.domain.base.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@TableName("t_forum")
@ApiModel(value = "Forum对象", description = "板块")
@Data
@EqualsAndHashCode(callSuper = true)
public class Forum extends BaseEntity {
    private String name;

    private String description;

    @ApiModelProperty("板块分类")
    private Long categoryId;
}
