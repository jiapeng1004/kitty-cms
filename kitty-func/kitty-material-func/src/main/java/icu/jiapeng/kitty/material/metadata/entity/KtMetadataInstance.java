package icu.jiapeng.kitty.material.metadata.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import icu.jiapeng.kitty.common.core.entity.CommonEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 编目实例 EAV（与 DDL metadata_instance 一致）。
 */
@Data
@TableName("kt_metadata_instance")
@EqualsAndHashCode(callSuper = true)
public class KtMetadataInstance extends CommonEntity {

    @TableField("resource_id")
    private String resourceId;

    @TableField("template_id")
    private String templateId;

    @TableField("field_id")
    private String fieldId;

    @TableField("field_value")
    private String fieldValue;

    @TableField("version")
    private Integer version;

    @TableField("last_version")
    private Integer lastVersion;
}
