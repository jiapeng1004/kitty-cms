package icu.jiapeng.kitty.material.catalog.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import icu.jiapeng.kitty.common.core.entity.CommonEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 栏目权限持久化实体。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("kt_catalog_permission")
public class KtCatalogPermission extends CommonEntity {

    @TableField("catalog_id")
    private String catalogId;

    @TableField("permission_code")
    private String permissionCode;

    @TableField("role_id")
    private String roleId;

    @TableField("allow_flag")
    private Boolean allowFlag;

    @TableField("editable")
    private Boolean editable;
}
