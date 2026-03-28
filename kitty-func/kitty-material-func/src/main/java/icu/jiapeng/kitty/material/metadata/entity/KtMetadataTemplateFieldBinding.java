package icu.jiapeng.kitty.material.metadata.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import icu.jiapeng.kitty.common.core.entity.CommonEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 模板字段绑定（与 DDL metadata_template_field_bind 一致）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("kt_metadata_template_field_bind")
public class KtMetadataTemplateFieldBinding extends CommonEntity {

    @TableField("template_id")
    private String templateId;

    @TableField("field_id")
    private String fieldId;

    @TableField("sort_num")
    private Integer sortNum;
}
