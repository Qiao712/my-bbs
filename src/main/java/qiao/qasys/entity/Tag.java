package qiao.qasys.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import qiao.qasys.common.AddGroup;
import qiao.qasys.common.BaseEntity;
import qiao.qasys.common.UpdateGroup;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Null;

@TableName("t_tag")
@ApiModel(value = "Tag对象", description = "标签")
@Data
@EqualsAndHashCode(callSuper = true)
public class Tag extends BaseEntity {
    @NotBlank(groups = AddGroup.class, message = "名称不可为空")
    @Length(groups = {AddGroup.class, UpdateGroup.class}, min = 1, max = 12, message = "标签名称长度应在1到12之间")
    private String name;

    @Length(groups = {AddGroup.class, UpdateGroup.class}, min = 1, max = 100, message = "标签描述长度应在1到100之间")
    private String description;

    @ApiModelProperty("标签类别名称")
    @Length(groups = {AddGroup.class, UpdateGroup.class}, min = 1, max = 10, message = "分类名称长度应在1到10之间")
    private String category;

    @ApiModelProperty("logo图片文件url")
    @Null(groups = {AddGroup.class, UpdateGroup.class}, message = "禁止直接指定logo图片url")
    private String logoUrl;
}
