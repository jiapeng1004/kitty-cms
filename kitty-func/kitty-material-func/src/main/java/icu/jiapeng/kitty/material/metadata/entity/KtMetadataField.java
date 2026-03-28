package icu.jiapeng.kitty.material.metadata.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import icu.jiapeng.kitty.common.core.entity.CommonEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 编目字段定义持久化实体（与 DDL metadata_field 一致）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("kt_metadata_field")
public class KtMetadataField extends CommonEntity {

    @TableField("field_code")
    private String fieldCode;

    @TableField("field_name")
    private String fieldName;

    @TableField("input_type")
    private String inputType;

    @TableField("required")
    private Integer required;

    @TableField("options_json")
    private String optionsJson;
}
