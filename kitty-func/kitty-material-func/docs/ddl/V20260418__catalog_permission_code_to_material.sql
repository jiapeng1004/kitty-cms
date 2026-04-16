-- 将栏目权限表中的短码迁移为与 MaterialPermissionCode 一致的编码（幂等：仅当旧值存在时更新）。

UPDATE `kt_catalog_permission`
SET `permission_code` = 'material:catalog:tree:view'
WHERE `permission_code` = 'view';

UPDATE `kt_catalog_permission`
SET `permission_code` = 'material:catalog:update'
WHERE `permission_code` = 'edit';

UPDATE `kt_catalog_permission`
SET `permission_code` = 'material:catalog:create'
WHERE `permission_code` = 'add';

UPDATE `kt_catalog_permission`
SET `permission_code` = 'material:catalog:delete'
WHERE `permission_code` = 'delete';
