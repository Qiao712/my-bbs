package sdu.addd.qasys.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import sdu.addd.qasys.common.AddGroup;
import sdu.addd.qasys.common.BaseEntity;
import sdu.addd.qasys.common.UpdateGroup;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Null;

@TableName("t_tag_relation")
@ApiModel(value = "TagRelation对象", description = "标签与问题的关联")
@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
public class TagRelation extends BaseEntity {
    private Long tagId;

    private Long questionId;
}
