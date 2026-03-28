package icu.jiapeng.kitty.material.metadata.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import icu.jiapeng.kitty.common.core.entity.CommonEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 编目模板持久化实体（与 DDL metadata_template 一致）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("kt_metadata_template")
public class KtMetadataTemplate extends CommonEntity {

    @TableField("name")
    private String name;

    @TableField("catalog_id")
    private String catalogId;

    @TableField("resource_type")
    private Integer resourceType;

    @TableField("enabled")
    private Integer enabled;
}
