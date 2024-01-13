package qiao.qasys.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import qiao.qasys.common.BaseEntity;

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
