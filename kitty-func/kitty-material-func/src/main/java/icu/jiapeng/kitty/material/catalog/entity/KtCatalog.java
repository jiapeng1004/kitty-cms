package icu.jiapeng.kitty.material.catalog.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import icu.jiapeng.kitty.common.core.entity.CommonEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 栏目持久化实体
 * 对应数据库表：kt_catalog
 * 用于存储栏目层级、归属、排序等核心信息
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("kt_catalog")
public class KtCatalog extends CommonEntity {

    @TableField("parent_id")
    private String parentId;

    @TableField("name")
    private String name;

    @TableField("scope_type")
    private String scopeType;

    @TableField("owner_user_id")
    private String ownerUserId;

    @TableField("sort_num")
    private Integer sortNum;

    @TableField("tree_code")
    private String treeCode;
}
