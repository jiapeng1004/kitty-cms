package icu.jiapeng.kitty.material.catalog.constants;

import icu.jiapeng.kitty.material.permission.constants.MaterialPermissionCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 栏目维度校验时使用的权限枚举；{@link #permissionCode} 与 {@link MaterialPermissionCode} 中对应常量一致。
 *
 * @author jiapeng
 * @since 2026/3/28
 */
@Getter
@AllArgsConstructor
public enum CatalogPermission {
    /**
     * 栏目树查看（出现在树中的可见性）。
     */
    TREE_VIEW(MaterialPermissionCode.MATERIAL_CATALOG_TREE_VIEW, "栏目树查看"),
    /**
     * 栏目信息修改（重命名、移动等）。
     */
    CATALOG_UPDATE(MaterialPermissionCode.MATERIAL_CATALOG_UPDATE, "栏目修改"),
    /**
     * 栏目删除。
     */
    CATALOG_DELETE(MaterialPermissionCode.MATERIAL_CATALOG_DELETE, "栏目删除"),
    /**
     * 新建子栏目。
     */
    CATALOG_CREATE(MaterialPermissionCode.MATERIAL_CATALOG_CREATE, "新建栏目"),
    /**
     * 编目模板管理（创建/绑定/启停）。
     */
    METADATA_TEMPLATE_MANAGE(MaterialPermissionCode.MATERIAL_METADATA_TEMPLATE_MANAGE, "编目模板管理"),
    /**
     * 资源列表查看。
     */
    RESOURCE_LIST_VIEW(MaterialPermissionCode.MATERIAL_RESOURCE_LIST_VIEW, "资源列表查看"),
    /**
     * 资源创建。
     */
    RESOURCE_CREATE(MaterialPermissionCode.MATERIAL_RESOURCE_CREATE, "资源创建"),
    /**
     * 资源更新。
     */
    RESOURCE_UPDATE(MaterialPermissionCode.MATERIAL_RESOURCE_UPDATE, "资源更新"),
    /**
     * 转码策略与栏目绑定管理。
     */
    TRANSCODE_POLICY_MANAGE(MaterialPermissionCode.MATERIAL_TRANSCODE_POLICY_MANAGE, "转码策略管理"),
    /**
     * 通用审核提交。
     */
    REVIEW_SUBMIT(MaterialPermissionCode.MATERIAL_REVIEW_SUBMIT, "审核提交"),
    /**
     * 通用审核处理（通过/拒绝）。
     */
    REVIEW_APPROVE(MaterialPermissionCode.MATERIAL_REVIEW_APPROVE, "审核处理"),
    ;

    private final String permissionCode;

    private final String permissionDesc;
}
