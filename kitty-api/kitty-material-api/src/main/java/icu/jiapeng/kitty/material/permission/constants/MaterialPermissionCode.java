package icu.jiapeng.kitty.material.permission.constants;

/**
 * material 模块权限编码。
 */
public interface MaterialPermissionCode {

    /**
     * 栏目树查看。
     */
    String MATERIAL_CATALOG_TREE_VIEW = "material:catalog:tree:view";

    /**
     * 资源列表查看。
     */
    String MATERIAL_RESOURCE_LIST_VIEW = "material:resource:list:view";

    /**
     * 资源创建。
     */
    String MATERIAL_RESOURCE_CREATE = "material:resource:create";

    /**
     * 资源更新。
     */
    String MATERIAL_RESOURCE_UPDATE = "material:resource:update";

    /**
     * 编目模板管理（创建/绑定/启停）。
     */
    String MATERIAL_METADATA_TEMPLATE_MANAGE = "material:metadata:template:manage";

    /**
     * 编目字段定义管理。
     */
    String MATERIAL_METADATA_FIELD_MANAGE = "material:metadata:field:manage";

    /**
     * 转码策略与栏目绑定管理。
     */
    String MATERIAL_TRANSCODE_POLICY_MANAGE = "material:transcode:policy:manage";

    /**
     * 站内信发送。
     */
    String MATERIAL_MESSAGE_SEND = "material:message:send";

    /**
     * 站内信查看（收件箱、SSE）。
     */
    String MATERIAL_MESSAGE_READ = "material:message:read";

    /**
     * 通用审核提交。
     */
    String MATERIAL_REVIEW_SUBMIT = "material:review:submit";

    /**
     * 通用审核处理（通过/拒绝）。
     */
    String MATERIAL_REVIEW_APPROVE = "material:review:approve";

    /**
     * 栏目权限管理。
     */
    String MATERIAL_CATALOG_PERMISSION_EDIT = "material:catalog:permission:edit";

    /**
     * 栏目权限的查看
     */
    String MATERIAL_CATALOG_PERMISSION_VIEW = "material:catalog:permission:view";
    /**
     * 新建栏目权限
     */
    String MATERIAL_CATALOG_CREATE = "material:catalog:create";

    /**
     * 栏目信息修改（重命名、移动等）。
     */
    String MATERIAL_CATALOG_UPDATE = "material:catalog:update";

    /**
     * 栏目删除。
     */
    String MATERIAL_CATALOG_DELETE = "material:catalog:delete";

    /**
     * 文件存储配置管理（增删改查 kt_file_storage，含 S3 等引擎）。
     */
    String MATERIAL_STORAGE_MANAGE = "material:storage:manage";
}
