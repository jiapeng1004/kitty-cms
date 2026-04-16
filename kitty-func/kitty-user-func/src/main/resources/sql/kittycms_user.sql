CREATE DATABASE IF NOT EXISTS kitty_user;
USE kitty_user;

DROP TABLE IF EXISTS kt_role_permission;
CREATE TABLE `kt_role_permission`
(
    `id`          VARCHAR(255) PRIMARY KEY,
    `role_id`     VARCHAR(255) NULL DEFAULT NULL,
    `p_code`      VARCHAR(255) NULL DEFAULT NULL,
    `create_time` TIMESTAMP    NULL DEFAULT NULL,
    `creator`     VARCHAR(255) NULL DEFAULT NULL,
    `update_time` TIMESTAMP    NULL DEFAULT NULL,
    `updater`     VARCHAR(255) NULL DEFAULT NULL
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

DROP TABLE IF EXISTS kt_oauth2_client;
CREATE TABLE `kt_oauth2_client`
(
    `id`                            VARCHAR(255) PRIMARY KEY,
    `client_name`                   VARCHAR(255) NULL DEFAULT NULL,
    `client_id`                     VARCHAR(255) NULL DEFAULT NULL,
    `client_secret`                 VARCHAR(255) NULL DEFAULT NULL,
    `allowed_scopes`                VARCHAR(255) NULL DEFAULT NULL,
    `allowed_grant_types`           VARCHAR(255) NULL DEFAULT NULL,
    `allow_authentication_methods`  VARCHAR(255) NULL DEFAULT NULL,
    `allowed_redirect_uris`         VARCHAR(255) NULL DEFAULT NULL,
    `access_token_timeout`          BIGINT       NULL DEFAULT NULL,
    `refresh_token_timeout`         BIGINT       NULL DEFAULT NULL,
    `black_list_exemption`          VARCHAR(255) NULL DEFAULT NULL,
    `status`                        INT          NULL DEFAULT NULL,
    `require_authorization_consent` INT          NULL DEFAULT NULL,
    `create_time`                   TIMESTAMP    NULL DEFAULT NULL,
    `creator`                       VARCHAR(255) NULL DEFAULT NULL,
    `update_time`                   TIMESTAMP    NULL DEFAULT NULL,
    `updater`                       VARCHAR(255) NULL DEFAULT NULL
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

DROP TABLE IF EXISTS kt_oauth2_scope;
CREATE TABLE `kt_oauth2_scope`
(
    `id`          VARCHAR(255) PRIMARY KEY,
    `scope_name`  VARCHAR(255) NULL DEFAULT NULL,
    `scope_code`  VARCHAR(255) NULL DEFAULT NULL,
    `scope_desc`  VARCHAR(255) NULL DEFAULT NULL,
    `create_time` TIMESTAMP    NULL DEFAULT NULL,
    `creator`     VARCHAR(255) NULL DEFAULT NULL,
    `update_time` TIMESTAMP    NULL DEFAULT NULL,
    `updater`     VARCHAR(255) NULL DEFAULT NULL,
    UNIQUE KEY `uk_oauth2_scope_code` (`scope_code`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

DROP TABLE IF EXISTS kt_role;
CREATE TABLE `kt_role`
(
    `id`          VARCHAR(255) PRIMARY KEY,
    `role_name`   VARCHAR(255) NULL DEFAULT NULL,
    `create_time` TIMESTAMP    NULL DEFAULT NULL,
    `creator`     VARCHAR(255) NULL DEFAULT NULL,
    `update_time` TIMESTAMP    NULL DEFAULT NULL,
    `updater`     VARCHAR(255) NULL DEFAULT NULL
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

DROP TABLE IF EXISTS kt_role_menu;
CREATE TABLE `kt_role_menu`
(
    `id`          VARCHAR(255) PRIMARY KEY,
    `role_id`     VARCHAR(255) NULL DEFAULT NULL,
    `menu_id`     VARCHAR(255) NULL DEFAULT NULL,
    `create_time` TIMESTAMP    NULL DEFAULT NULL,
    `creator`     VARCHAR(255) NULL DEFAULT NULL,
    `update_time` TIMESTAMP    NULL DEFAULT NULL,
    `updater`     VARCHAR(255) NULL DEFAULT NULL
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

DROP TABLE IF EXISTS kt_user;
CREATE TABLE `kt_user`
(
    `id`          VARCHAR(255) PRIMARY KEY,
    `real_name`   VARCHAR(255) NULL DEFAULT NULL,
    `nick_name`   VARCHAR(255) NULL DEFAULT NULL,
    `pwd`         VARCHAR(255) NULL DEFAULT NULL,
    `phone`       VARCHAR(255) NULL DEFAULT NULL,
    `email`       VARCHAR(255) NULL DEFAULT NULL,
    `id_card`     VARCHAR(255) NULL DEFAULT NULL,
    `status`      INT          NULL DEFAULT NULL,
    `deleted`     INT          NULL DEFAULT NULL,
    `create_time` TIMESTAMP    NULL DEFAULT NULL,
    `creator`     VARCHAR(255) NULL DEFAULT NULL,
    `update_time` TIMESTAMP    NULL DEFAULT NULL,
    `updater`     VARCHAR(255) NULL DEFAULT NULL
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

DROP TABLE IF EXISTS kt_user_group;
CREATE TABLE `kt_user_group`
(
    `id`          VARCHAR(255) PRIMARY KEY,
    `group_name`  VARCHAR(255) NULL DEFAULT NULL,
    `tenant_id`   VARCHAR(255) NULL DEFAULT NULL,
    `create_time` TIMESTAMP    NULL DEFAULT NULL,
    `creator`     VARCHAR(255) NULL DEFAULT NULL,
    `update_time` TIMESTAMP    NULL DEFAULT NULL,
    `updater`     VARCHAR(255) NULL DEFAULT NULL
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

DROP TABLE IF EXISTS kt_menu;
CREATE TABLE `kt_menu`
(
    `id`          VARCHAR(255) PRIMARY KEY,
    `parent_id`   VARCHAR(255) NULL DEFAULT NULL,
    `menu_name`   VARCHAR(255) NULL DEFAULT NULL,
    `menu_type`   VARCHAR(16)  NULL DEFAULT 'MENU',
    `menu_key`    VARCHAR(255) NULL DEFAULT NULL,
    `path`        VARCHAR(255) NULL DEFAULT NULL,
    `icon`        VARCHAR(255) NULL DEFAULT NULL,
    `component`   VARCHAR(255) NULL DEFAULT NULL,
    `link_type`   VARCHAR(16)  NULL DEFAULT NULL,
    `link_url`    VARCHAR(512) NULL DEFAULT NULL,
    `sort`        INT          NULL DEFAULT NULL,
    `p_codes`     VARCHAR(255) NULL DEFAULT NULL,
    `enabled`     INT          NULL DEFAULT NULL,
    `create_time` TIMESTAMP    NULL DEFAULT NULL,
    `creator`     VARCHAR(255) NULL DEFAULT NULL,
    `update_time` TIMESTAMP    NULL DEFAULT NULL,
    `updater`     VARCHAR(255) NULL DEFAULT NULL
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

DROP TABLE IF EXISTS kt_aksk_client;
CREATE TABLE `kt_aksk_client`
(
    `id`             VARCHAR(255) PRIMARY KEY,
    `client_name`    VARCHAR(255) NULL DEFAULT NULL,
    `access_key`     VARCHAR(255) NULL DEFAULT NULL,
    `secret_key`     VARCHAR(255) NULL DEFAULT NULL,
    `allowed_scopes` VARCHAR(255) NULL DEFAULT NULL,
    `status`         INT          NULL DEFAULT NULL,
    `create_time`    TIMESTAMP    NULL DEFAULT NULL,
    `creator`        VARCHAR(255) NULL DEFAULT NULL,
    `update_time`    TIMESTAMP    NULL DEFAULT NULL,
    `updater`        VARCHAR(255) NULL DEFAULT NULL
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

DROP TABLE IF EXISTS kt_permission;
CREATE TABLE `kt_permission`
(
    `id`          VARCHAR(255) PRIMARY KEY,
    `p_name`      VARCHAR(255) NULL DEFAULT NULL,
    `p_code`      VARCHAR(255) NULL DEFAULT NULL,
    `p_desc`      VARCHAR(255) NULL DEFAULT NULL,
    `create_time` TIMESTAMP    NULL DEFAULT NULL,
    `creator`     VARCHAR(255) NULL DEFAULT NULL,
    `update_time` TIMESTAMP    NULL DEFAULT NULL,
    `updater`     VARCHAR(255) NULL DEFAULT NULL
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

DROP TABLE IF EXISTS kt_tenant;
CREATE TABLE `kt_tenant`
(
    `id`          VARCHAR(255) PRIMARY KEY,
    `name`        VARCHAR(255) NULL DEFAULT NULL,
    `create_time` TIMESTAMP    NULL DEFAULT NULL,
    `creator`     VARCHAR(255) NULL DEFAULT NULL,
    `update_time` TIMESTAMP    NULL DEFAULT NULL,
    `updater`     VARCHAR(255) NULL DEFAULT NULL
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

DROP TABLE IF EXISTS kt_config;
CREATE TABLE `kt_config`
(
    `id`             VARCHAR(255) PRIMARY KEY,
    `config_name`    VARCHAR(255) NOT NULL,
    `config_key`     VARCHAR(255) NOT NULL,
    `config_desc`    VARCHAR(255) NULL DEFAULT NULL,
    `config_way`     VARCHAR(255) NOT NULL,
    `config_enum`    VARCHAR(255) NULL DEFAULT NULL,
    `config_value`   VARCHAR(255) NULL DEFAULT NULL,
    `config_default` VARCHAR(255) NULL DEFAULT NULL,
    `class_id`       VARCHAR(255) NULL DEFAULT NULL,
    `create_time`    TIMESTAMP    NULL DEFAULT NULL,
    `creator`        VARCHAR(255) NULL DEFAULT NULL,
    `update_time`    TIMESTAMP    NULL DEFAULT NULL,
    `updater`        VARCHAR(255) NULL DEFAULT NULL
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

DROP TABLE IF EXISTS kt_user_role;
CREATE TABLE `kt_user_role`
(
    `id`          VARCHAR(255) PRIMARY KEY,
    `user_id`     VARCHAR(255) NULL DEFAULT NULL,
    `role_id`     VARCHAR(255) NULL DEFAULT NULL,
    `create_time` TIMESTAMP    NULL DEFAULT NULL,
    `creator`     VARCHAR(255) NULL DEFAULT NULL,
    `update_time` TIMESTAMP    NULL DEFAULT NULL,
    `updater`     VARCHAR(255) NULL DEFAULT NULL
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

DROP TABLE IF EXISTS kt_open_user;
CREATE TABLE `kt_open_user`
(
    `id`          VARCHAR(255) PRIMARY KEY,
    `source`      VARCHAR(255) NULL DEFAULT NULL,
    `open_uid`    VARCHAR(255) NULL DEFAULT NULL,
    `user_id`     VARCHAR(255) NULL DEFAULT NULL,
    `extra`       VARCHAR(255) NULL DEFAULT NULL,
    `create_time` TIMESTAMP    NULL DEFAULT NULL,
    `creator`     VARCHAR(255) NULL DEFAULT NULL,
    `update_time` TIMESTAMP    NULL DEFAULT NULL,
    `updater`     VARCHAR(255) NULL DEFAULT NULL
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

DROP TABLE IF EXISTS kt_user_attr;
CREATE TABLE `kt_user_attr`
(
    `id`          VARCHAR(255) PRIMARY KEY,
    `user_id`     VARCHAR(255) NULL DEFAULT NULL,
    `attr_key`    VARCHAR(255) NULL DEFAULT NULL,
    `attr_value`  VARCHAR(255) NULL DEFAULT NULL,
    `create_time` TIMESTAMP    NULL DEFAULT NULL,
    `creator`     VARCHAR(255) NULL DEFAULT NULL,
    `update_time` TIMESTAMP    NULL DEFAULT NULL,
    `updater`     VARCHAR(255) NULL DEFAULT NULL
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

DROP TABLE IF EXISTS kt_config_class;
CREATE TABLE `kt_config_class`
(
    `id`          VARCHAR(255) PRIMARY KEY,
    `class_name`  VARCHAR(255) NULL DEFAULT NULL,
    `class_desc`  VARCHAR(255) NULL DEFAULT NULL,
    `owner`       VARCHAR(255) NULL DEFAULT NULL,
    `create_time` TIMESTAMP    NULL DEFAULT NULL,
    `creator`     VARCHAR(255) NULL DEFAULT NULL,
    `update_time` TIMESTAMP    NULL DEFAULT NULL,
    `updater`     VARCHAR(255) NULL DEFAULT NULL
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- 基线配置分类：验证码/登录相关
INSERT INTO `kt_config_class` (`id`, `class_name`, `class_desc`, `owner`, `create_time`, `creator`, `update_time`,
                               `updater`)
VALUES ('config-class-captcha', '验证码配置', '登录验证码等配置', 'public', NOW(), 'system', NOW(), 'system')
ON DUPLICATE KEY UPDATE `class_name` = VALUES(`class_name`),
                        `class_desc` = VALUES(`class_desc`);

-- 基线配置分类：OAuth2 客户端（飞书、钉钉等第三方登录）
INSERT INTO `kt_config_class` (`id`, `class_name`, `class_desc`, `owner`, `create_time`, `creator`, `update_time`,
                               `updater`)
VALUES ('config-class-oauth2', 'OAuth2 客户端配置', '第三方登录客户端 ID、密钥、回调地址等', 'public', NOW(), 'system',
        NOW(), 'system')
ON DUPLICATE KEY UPDATE `class_name` = VALUES(`class_name`),
                        `class_desc` = VALUES(`class_desc`);


-- 基线配置：OAuth2 各平台客户端（默认空串，需在管理端或库内填写后生效）
INSERT INTO `kt_config` (`id`, `config_name`, `config_key`, `config_desc`, `config_way`, `config_value`,
                         `config_default`, `class_id`, `create_time`, `creator`, `update_time`, `updater`)
VALUES ('oauth2-feishu-client-id', '飞书 App ID', 'oauth2.feishu.client_id', '飞书开放平台应用 App ID', 'TEXT', '', '',
        'config-class-oauth2', NOW(), 'system', NOW(), 'system'),
       ('oauth2-feishu-client-secret', '飞书 App Secret', 'oauth2.feishu.client_secret', '飞书开放平台应用 App Secret',
        'TEXT', '', '', 'config-class-oauth2', NOW(), 'system', NOW(), 'system'),
       ('oauth2-dingtalk-client-id', '钉钉 API Key', 'oauth2.dingtalk.client_id', '钉钉扫码登录应用 API Key', 'TEXT',
        '', '', 'config-class-oauth2', NOW(), 'system', NOW(), 'system'),
       ('oauth2-dingtalk-client-secret', '钉钉 Secret', 'oauth2.dingtalk.client_secret', '钉钉扫码登录应用 Secret',
        'TEXT', '', '', 'config-class-oauth2', NOW(), 'system', NOW(), 'system'),
       ('oauth2-github-client-id', 'GitHub Client ID', 'oauth2.github.client_id', 'GitHub OAuth App Client ID', 'TEXT',
        '', '', 'config-class-oauth2', NOW(), 'system', NOW(), 'system'),
       ('oauth2-github-client-secret', 'GitHub Client Secret', 'oauth2.github.client_secret',
        'GitHub OAuth App Client Secret', 'TEXT', '', '', 'config-class-oauth2', NOW(), 'system', NOW(), 'system'),
       ('oauth2-google-client-id', 'Google Client ID', 'oauth2.google.client_id', 'Google OAuth 2.0 Client ID', 'TEXT',
        '', '', 'config-class-oauth2', NOW(), 'system', NOW(), 'system'),
       ('oauth2-google-client-secret', 'Google Client Secret', 'oauth2.google.client_secret',
        'Google OAuth 2.0 Client Secret', 'TEXT', '', '', 'config-class-oauth2', NOW(), 'system', NOW(), 'system'),
       ('oauth2-microsoft-client-id', 'Microsoft Client ID', 'oauth2.microsoft.client_id',
        'Microsoft Entra ID 应用(客户端) ID', 'TEXT', '', '', 'config-class-oauth2', NOW(), 'system', NOW(), 'system'),
       ('oauth2-microsoft-client-secret', 'Microsoft Client Secret', 'oauth2.microsoft.client_secret',
        'Microsoft 客户端密码', 'TEXT', '', '', 'config-class-oauth2', NOW(), 'system', NOW(), 'system'),
       ('oauth2-wechat-client-id', '微信 AppID', 'oauth2.wechat.client_id', '微信开放平台网站应用 AppID', 'TEXT', '',
        '', 'config-class-oauth2', NOW(), 'system', NOW(), 'system'),
       ('oauth2-wechat-client-secret', '微信 AppSecret', 'oauth2.wechat.client_secret',
        '微信开放平台网站应用 AppSecret', 'TEXT', '', '', 'config-class-oauth2', NOW(), 'system', NOW(), 'system'),
       ('oauth2-redirect-after-login', 'OAuth2 登录后跳转', 'oauth2.redirect_after_login',
        '第三方登录成功后跳转的前端地址（含协议与域名），为空则相对路径 /login', 'TEXT', '', '', 'config-class-oauth2',
        NOW(), 'system', NOW(), 'system'),
       ('user-captcha-service-type', '验证码服务类型', 'user.captcha.service.type',
        '登录验证码实现类型，如 hutool', 'TEXT', 'hutool', 'hutool', 'config-class-captcha', NOW(),
        'system', NOW(), 'system')
ON DUPLICATE KEY UPDATE `config_value`   = VALUES(`config_value`),
                        `config_default` = VALUES(`config_default`),
                        `class_id`       = VALUES(`class_id`);


-- 初始化系统管理员账号（密码在应用启动时用 BCrypt 计算并写入，此处先留空）
INSERT INTO `kt_user` (`id`, `real_name`, `nick_name`, `pwd`, `phone`, `email`, `status`, `deleted`, `create_time`,
                       `creator`, `update_time`, `updater`)
-- 密码123456aB
VALUES ('user-admin', '系统管理员', 'admin', '$2b$10$13j1BBe.dWPgXVPVuyOprO1I6U.0Iq5PFRe19GiCJj4kEbai4.5ra', NULL, NULL,
        1, 0, NOW(), 'system', NOW(), 'system')
ON DUPLICATE KEY UPDATE `nick_name` = VALUES(`nick_name`);

-- 初始化系统管理员角色
INSERT INTO `kt_role` (`id`, `role_name`, `create_time`, `creator`, `update_time`, `updater`)
VALUES ('role-admin', '系统管理员', NOW(), 'system', NOW(), 'system')
ON DUPLICATE KEY UPDATE `role_name` = VALUES(`role_name`);


-- 将系统管理员账号绑定到系统管理员角色
INSERT INTO `kt_user_role` (`id`, `role_id`, `user_id`, `create_time`, `creator`, `update_time`, `updater`)
VALUES ('user-role-admin', 'role-admin', 'user-admin', NOW(), 'system', NOW(), 'system')
ON DUPLICATE KEY UPDATE `role_id` = VALUES(`role_id`),
                        `user_id` = VALUES(`user_id`);


-- 为系统管理员角色授予全部权限
INSERT INTO `kt_role_permission` (`id`, `role_id`, `p_code`, `create_time`, `creator`, `update_time`, `updater`)
SELECT CONCAT('rp-admin-', p.p_code) AS id,
       'role-admin'                  AS role_id,
       p.p_code,
       NOW(),
       'system',
       NOW(),
       'system'
FROM kt_permission p
         LEFT JOIN kt_role_permission rp
                   ON rp.role_id = 'role-admin' AND rp.p_code = p.p_code
WHERE rp.id IS NULL;

-- 初始化权限：与 icu.jiapeng.kitty.user.permission.constants.KtPermissionCode 对应
INSERT INTO `kt_permission` (`id`, `p_name`, `p_code`, `p_desc`, `create_time`, `creator`, `update_time`, `updater`)
VALUES ('perm-system-dashboard-view', '查看工作台', 'system:dashboard:view', '查看后台工作台总览', NOW(), 'system',
        NOW(), 'system'),
       ('perm-config-view', '查看配置', 'config:view', '查看配置列表和详情', NOW(), 'system', NOW(), 'system'),
       ('perm-config-create', '新增配置', 'config:create', '创建新的配置项', NOW(), 'system', NOW(), 'system'),
       ('perm-config-set', '设置配置值', 'config:set', '仅修改配置的当前值', NOW(), 'system', NOW(), 'system'),
       ('perm-config-update', '编辑配置', 'config:update', '编辑配置元信息', NOW(), 'system', NOW(), 'system'),
       ('perm-config-delete', '删除配置', 'config:delete', '删除配置项', NOW(), 'system', NOW(), 'system'),
       ('perm-oauth2-client-view', '查看 OAuth2 客户端', 'oauth2:client:view', '查看第三方 OAuth2 客户端', NOW(),
        'system', NOW(), 'system'),
       ('perm-oauth2-client-create', '新增 OAuth2 客户端', 'oauth2:client:create', '创建第三方 OAuth2 客户端', NOW(),
        'system', NOW(), 'system'),
       ('perm-oauth2-client-update', '编辑 OAuth2 客户端', 'oauth2:client:update', '编辑第三方 OAuth2 客户端', NOW(),
        'system', NOW(), 'system'),
       ('perm-oauth2-client-delete', '删除 OAuth2 客户端', 'oauth2:client:delete', '删除第三方 OAuth2 客户端', NOW(),
        'system', NOW(), 'system'),
       ('perm-oauth2-scope-view', '查看 OAuth2 Scope', 'oauth2:scope:view', '查看 OAuth2 scope 定义列表', NOW(),
        'system', NOW(), 'system'),
       ('perm-menu-view', '查看菜单', 'menu:view', '查看后台菜单配置', NOW(), 'system', NOW(), 'system'),
       ('perm-menu-create', '新增菜单', 'menu:create', '创建后台菜单', NOW(), 'system', NOW(), 'system'),
       ('perm-menu-update', '编辑菜单', 'menu:update', '编辑后台菜单', NOW(), 'system', NOW(), 'system'),
       ('perm-menu-delete', '删除菜单', 'menu:delete', '删除后台菜单', NOW(), 'system', NOW(), 'system'),
       ('perm-permission-view', '查看权限', 'permission:view', '查看权限定义', NOW(), 'system', NOW(), 'system'),
       ('perm-permission-create', '新增权限', 'permission:create', '新增权限定义', NOW(), 'system', NOW(), 'system'),
       ('perm-permission-update', '编辑权限', 'permission:update', '编辑权限定义', NOW(), 'system', NOW(), 'system'),
       ('perm-permission-delete', '删除权限', 'permission:delete', '删除权限定义', NOW(), 'system', NOW(), 'system'),
       ('perm-role-view', '查看角色', 'role:view', '查看角色列表和详情', NOW(), 'system', NOW(), 'system'),
       ('perm-role-create', '新增角色', 'role:create', '创建角色', NOW(), 'system', NOW(), 'system'),
       ('perm-role-update', '编辑角色', 'role:update', '编辑角色', NOW(), 'system', NOW(), 'system'),
       ('perm-role-delete', '删除角色', 'role:delete', '删除角色', NOW(), 'system', NOW(), 'system'),
       ('perm-role-permission-view', '查看角色权限', 'role:permission:view', '查看角色绑定的权限', NOW(), 'system',
        NOW(), 'system'),
       ('perm-role-permission-update', '编辑角色权限', 'role:permission:update', '为角色分配权限', NOW(), 'system',
        NOW(), 'system')
ON DUPLICATE KEY UPDATE `p_name` =
                            VALUES
                            (`p_name`),
                        `p_desc` =
                            VALUES
                            (`p_desc`);

-- mam的权限
-- 插入素材模块权限
INSERT INTO `kt_permission` (`id`, `p_name`, `p_code`, `p_desc`, `create_time`, `creator`, `update_time`, `updater`)
VALUES ('perm-material-catalog-tree-view', '栏目树查看', 'material:catalog:tree:view', '查看素材栏目树结构', NOW(),
        'system', NOW(), 'system'),
       ('perm-material-resource-list-view', '资源列表查看', 'material:resource:list:view', '查看素材资源列表', NOW(),
        'system', NOW(), 'system'),
       ('perm-material-resource-create', '资源创建', 'material:resource:create', '创建新的素材资源', NOW(), 'system',
        NOW(), 'system'),
       ('perm-material-resource-update', '资源更新', 'material:resource:update', '更新素材资源信息', NOW(), 'system',
        NOW(), 'system'),
       ('perm-material-metadata-template-manage', '编目模板管理', 'material:metadata:template:manage',
        '创建/绑定/启停编目模板', NOW(), 'system', NOW(), 'system'),
       ('perm-material-metadata-field-manage', '编目字段定义管理', 'material:metadata:field:manage', '管理编目字段定义',
        NOW(), 'system', NOW(), 'system'),
       ('perm-material-transcode-policy-manage', '转码策略与栏目绑定管理', 'material:transcode:policy:manage',
        '管理转码策略与栏目绑定', NOW(), 'system', NOW(), 'system'),
       ('perm-material-message-send', '站内信发送', 'material:message:send', '发送站内消息', NOW(), 'system', NOW(),
        'system'),
       ('perm-material-message-read', '站内信查看', 'material:message:read', '查看收件箱和SSE消息', NOW(), 'system',
        NOW(), 'system'),
       ('perm-material-review-submit', '通用审核提交', 'material:review:submit', '提交审核请求', NOW(), 'system', NOW(),
        'system'),
       ('perm-material-review-approve', '通用审核处理', 'material:review:approve', '处理审核请求（通过/拒绝）', NOW(),
        'system', NOW(), 'system'),
       ('perm-material-catalog-permission-edit', '栏目权限管理', 'material:catalog:permission:edit', '编辑栏目权限',
        NOW(), 'system', NOW(), 'system'),
       ('perm-material-catalog-permission-view', '栏目权限查看', 'material:catalog:permission:view', '查看栏目权限',
        NOW(), 'system', NOW(), 'system'),
       ('perm-material-catalog-create', '新建栏目', 'material:catalog:create', '创建新的素材栏目', NOW(), 'system',
        NOW(), 'system'),
       ('perm-material-storage-manage', '文件存储配置', 'material:storage:manage', '管理对象存储/磁盘存储实例（kt_file_storage）',
        NOW(), 'system', NOW(), 'system')
ON DUPLICATE KEY UPDATE `p_name` = VALUES(`p_name`),
                        `p_desc` = VALUES(`p_desc`);

-- 初始化 OAuth2 scope：与 icu.jiapeng.kitty.user.auth.constants.KtOauth2Scope 对应
INSERT INTO `kt_oauth2_scope` (`id`, `scope_name`, `scope_code`, `scope_desc`, `create_time`, `creator`, `update_time`,
                               `updater`)
VALUES ('oauth2-scope-openid', 'OpenID', 'openid', 'OpenID Connect 身份标识 scope', NOW(), 'system', NOW(), 'system'),
       ('oauth2-scope-profile', '个人资料', 'profile', '用户基础资料 scope', NOW(), 'system', NOW(), 'system'),
       ('oauth2-scope-email', '邮箱', 'email', '用户邮箱 scope', NOW(), 'system', NOW(), 'system'),
       ('oauth2-scope-phone', '手机号', 'phone', '用户手机号 scope', NOW(), 'system', NOW(), 'system'),
       ('oauth2-scope-address', '地址', 'address', '用户地址 scope', NOW(), 'system', NOW(), 'system')
ON DUPLICATE KEY UPDATE `scope_name` = VALUES(`scope_name`),
                        `scope_desc` = VALUES(`scope_desc`);

-- 给超管角色所有的权限
INSERT INTO `kt_role_permission` (`id`, `role_id`, `p_code`, `create_time`, `creator`, `update_time`, `updater`)
SELECT CONCAT('rp-admin-', p.p_code) AS id,
       'role-admin'                  AS role_id,
       p.p_code,
       NOW(),
       'system',
       NOW(),
       'system'
FROM kt_permission p
         LEFT JOIN kt_role_permission rp
                   ON rp.role_id = 'role-admin' AND rp.p_code = p.p_code
WHERE rp.id IS NULL
ON DUPLICATE KEY UPDATE `p_code`=VALUES(`p_code`);


-- 基线菜单：与当前前端路由对应
INSERT INTO `kt_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `menu_key`, `path`, `icon`, `component`,
                       `link_type`,
                       `link_url`, `sort`, `p_codes`, `enabled`,
                       `create_time`, `creator`, `update_time`, `updater`)
VALUES ('menu-root-dashboard', NULL, '工作台', 'MENU', 'dashboard', '/', 'dashboard', 'Dashboard', NULL, NULL, 10,
        'system:dashboard:view', 1, NOW(), 'system', NOW(), 'system'),
       ('menu-root-system', NULL, '系统管理', 'DIR', 'system-root', NULL, 'setting', NULL, NULL, NULL, 15, NULL, 1,
        NOW(),
        'system', NOW(), 'system'),
       ('menu-root-config', 'menu-root-system', '配置管理', 'MENU', 'config', '/config', 'setting', 'config/ConfigList',
        NULL, NULL, 20, 'config:view', 1, NOW(), 'system', NOW(), 'system'),
       ('menu-root-config-class', 'menu-root-system', '配置分类', 'MENU', 'config-class', '/config-class', 'appstore',
        'config-class/ConfigClassList', NULL, NULL, 30, 'config:view', 1, NOW(), 'system', NOW(), 'system'),
       ('menu-root-oauth2-client', 'menu-root-system', 'OAuth2客户端', 'MENU', 'oauth2-client', '/oauth2-client',
        'setting', 'oauth2-client/Oauth2ClientList', NULL, NULL, 35, 'oauth2:client:view,oauth2:scope:view', 1, NOW(),
        'system', NOW(),
        'system'),
       ('menu-root-tenant', NULL, '租户管理', 'MENU', 'tenant', '/tenant', 'team', 'tenant/TenantList', NULL, NULL, 40,
        'tenant:view', 1, NOW(), 'system', NOW(), 'system'),
       ('menu-root-user', NULL, '用户管理', 'MENU', 'user', '/user', 'user', 'user/UserList', NULL, NULL, 50,
        'user:account:view', 1, NOW(), 'system', NOW(), 'system'),
       ('menu-root-role', NULL, '角色管理', 'MENU', 'role', '/role', 'id-badge', 'role/RoleList', NULL, NULL, 60,
        'role:view', 1, NOW(), 'system', NOW(), 'system')
ON DUPLICATE KEY UPDATE `menu_name` = VALUES(`menu_name`),
                        `menu_type` = VALUES(`menu_type`),
                        `path`      = VALUES(`path`),
                        `component` = VALUES(`component`),
                        `link_type` = VALUES(`link_type`),
                        `link_url`  = VALUES(`link_url`),
                        `p_codes`   = VALUES(`p_codes`),
                        `enabled`   = VALUES(`enabled`);

-- 给超管角色添加所有菜单
INSERT INTO `kt_role_menu` (`id`, `role_id`, `menu_id`, `create_time`, `creator`, `update_time`, `updater`)
SELECT CONCAT('rm-admin-', m.id) AS id,
       'role-admin'              AS role_id,
       m.id                      AS menu_id,
       NOW(),
       'system',
       NOW(),
       'system'
FROM kt_menu m
         LEFT JOIN kt_role_menu rm
                   ON rm.role_id = 'role-admin' AND rm.menu_id = m.id
WHERE rm.id IS NULL;