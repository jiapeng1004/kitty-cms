DROP TABLE IF EXISTS kt_user_aksk_client;
CREATE TABLE `kt_user_aksk_client`
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

DROP TABLE IF EXISTS kt_user_role_user;
CREATE TABLE `kt_user_role_user`
(
    `id`          VARCHAR(255) PRIMARY KEY,
    `role_id`     VARCHAR(255) NULL DEFAULT NULL,
    `user_id`     VARCHAR(255) NULL DEFAULT NULL,
    `create_time` TIMESTAMP    NULL DEFAULT NULL,
    `creator`     VARCHAR(255) NULL DEFAULT NULL,
    `update_time` TIMESTAMP    NULL DEFAULT NULL,
    `updater`     VARCHAR(255) NULL DEFAULT NULL
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

DROP TABLE IF EXISTS kt_user_oauth2_client;
CREATE TABLE `kt_user_oauth2_client`
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
ON DUPLICATE KEY UPDATE `class_name` = VALUES(`class_name`), `class_desc` = VALUES(`class_desc`);

-- 基线配置分类：OAuth2 客户端（飞书、钉钉等第三方登录）
INSERT INTO `kt_config_class` (`id`, `class_name`, `class_desc`, `owner`, `create_time`, `creator`, `update_time`,
                               `updater`)
VALUES ('config-class-oauth2', 'OAuth2 客户端配置', '第三方登录客户端 ID、密钥、回调地址等', 'public', NOW(), 'system', NOW(), 'system')
ON DUPLICATE KEY UPDATE `class_name` = VALUES(`class_name`), `class_desc` = VALUES(`class_desc`);

DROP TABLE IF EXISTS kt_user_user_attr;
CREATE TABLE `kt_user_user_attr`
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

DROP TABLE IF EXISTS kt_open_user;
CREATE TABLE `kt_open_user`
(
    `id`          VARCHAR(255) PRIMARY KEY,
    `source`      VARCHAR(64)  NOT NULL COMMENT '平台标识 feishu/dingtalk/github 等',
    `open_uid`    VARCHAR(255) NOT NULL COMMENT '上游 SSO 用户唯一标识',
    `user_id`     VARCHAR(255) NOT NULL COMMENT '本地用户 id',
    `extra`       TEXT         NULL DEFAULT NULL COMMENT '扩展属性 JSON',
    `create_time` TIMESTAMP    NULL DEFAULT NULL,
    `creator`     VARCHAR(255) NULL DEFAULT NULL,
    `update_time` TIMESTAMP    NULL DEFAULT NULL,
    `updater`     VARCHAR(255) NULL DEFAULT NULL,
    UNIQUE KEY `uk_source_open_uid` (`source`, `open_uid`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '开放认证用户：上游 SSO 与本地用户映射';

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

DROP TABLE IF EXISTS kt_user_role;
CREATE TABLE `kt_user_role`
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

DROP TABLE IF EXISTS kt_permission_owner;
CREATE TABLE `kt_permission_owner`
(
    `id`          VARCHAR(255) PRIMARY KEY,
    `owner_id`    VARCHAR(255) NULL DEFAULT NULL,
    `owner_type`  VARCHAR(255) NULL DEFAULT NULL,
    `p_code`      VARCHAR(255) NULL DEFAULT NULL,
    `create_time` TIMESTAMP    NULL DEFAULT NULL,
    `creator`     VARCHAR(255) NULL DEFAULT NULL,
    `update_time` TIMESTAMP    NULL DEFAULT NULL,
    `updater`     VARCHAR(255) NULL DEFAULT NULL
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

DROP TABLE IF EXISTS kt_user_user;
CREATE TABLE `kt_user_user`
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

DROP TABLE IF EXISTS kt_permission;
CREATE TABLE `kt_permission`
(
    `id`          VARCHAR(255) PRIMARY KEY,
    `p_name`      VARCHAR(255) NULL DEFAULT NULL,
    `p_code`      VARCHAR(255) NULL DEFAULT NULL,
    `p_type`      VARCHAR(255) NULL DEFAULT NULL,
    `p_desc`      VARCHAR(255) NULL DEFAULT NULL,
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

-- 基线配置：默认验证码服务类型（Hutool + Redis），归属配置分类 config-class-captcha
INSERT INTO `kt_config` (`id`, `config_name`, `config_key`, `config_desc`, `config_way`, `config_value`,
                         `config_default`, `class_id`, `create_time`, `creator`, `update_time`, `updater`)
VALUES ('user-captcha-service-type', '验证码服务类型', 'user.captcha.service.type',
        '登录验证码实现类型，如 hutool_redis', 'TEXT', 'hutool_redis', 'hutool_redis', 'config-class-captcha', NOW(),
        'system', NOW(), 'system')
ON DUPLICATE KEY UPDATE `config_value` = VALUES(`config_value`), `config_default` = VALUES(`config_default`),
                        `class_id` = VALUES(`class_id`);

-- 基线配置：OAuth2 各平台客户端（默认空串，需在管理端或库内填写后生效）
INSERT INTO `kt_config` (`id`, `config_name`, `config_key`, `config_desc`, `config_way`, `config_value`,
                         `config_default`, `class_id`, `create_time`, `creator`, `update_time`, `updater`)
VALUES
('oauth2-feishu-client-id', '飞书 App ID', 'oauth2.feishu.client_id', '飞书开放平台应用 App ID', 'TEXT', '', '', 'config-class-oauth2', NOW(), 'system', NOW(), 'system'),
('oauth2-feishu-client-secret', '飞书 App Secret', 'oauth2.feishu.client_secret', '飞书开放平台应用 App Secret', 'TEXT', '', '', 'config-class-oauth2', NOW(), 'system', NOW(), 'system'),
('oauth2-dingtalk-client-id', '钉钉 API Key', 'oauth2.dingtalk.client_id', '钉钉扫码登录应用 API Key', 'TEXT', '', '', 'config-class-oauth2', NOW(), 'system', NOW(), 'system'),
('oauth2-dingtalk-client-secret', '钉钉 Secret', 'oauth2.dingtalk.client_secret', '钉钉扫码登录应用 Secret', 'TEXT', '', '', 'config-class-oauth2', NOW(), 'system', NOW(), 'system'),
('oauth2-github-client-id', 'GitHub Client ID', 'oauth2.github.client_id', 'GitHub OAuth App Client ID', 'TEXT', '', '', 'config-class-oauth2', NOW(), 'system', NOW(), 'system'),
('oauth2-github-client-secret', 'GitHub Client Secret', 'oauth2.github.client_secret', 'GitHub OAuth App Client Secret', 'TEXT', '', '', 'config-class-oauth2', NOW(), 'system', NOW(), 'system'),
('oauth2-google-client-id', 'Google Client ID', 'oauth2.google.client_id', 'Google OAuth 2.0 Client ID', 'TEXT', '', '', 'config-class-oauth2', NOW(), 'system', NOW(), 'system'),
('oauth2-google-client-secret', 'Google Client Secret', 'oauth2.google.client_secret', 'Google OAuth 2.0 Client Secret', 'TEXT', '', '', 'config-class-oauth2', NOW(), 'system', NOW(), 'system'),
('oauth2-microsoft-client-id', 'Microsoft Client ID', 'oauth2.microsoft.client_id', 'Microsoft Entra ID 应用(客户端) ID', 'TEXT', '', '', 'config-class-oauth2', NOW(), 'system', NOW(), 'system'),
('oauth2-microsoft-client-secret', 'Microsoft Client Secret', 'oauth2.microsoft.client_secret', 'Microsoft 客户端密码', 'TEXT', '', '', 'config-class-oauth2', NOW(), 'system', NOW(), 'system'),
('oauth2-wechat-client-id', '微信 AppID', 'oauth2.wechat.client_id', '微信开放平台网站应用 AppID', 'TEXT', '', '', 'config-class-oauth2', NOW(), 'system', NOW(), 'system'),
('oauth2-wechat-client-secret', '微信 AppSecret', 'oauth2.wechat.client_secret', '微信开放平台网站应用 AppSecret', 'TEXT', '', '', 'config-class-oauth2', NOW(), 'system', NOW(), 'system'),
('oauth2-redirect-after-login', 'OAuth2 登录后跳转', 'oauth2.redirect_after_login', '第三方登录成功后跳转的前端地址（含协议与域名），为空则相对路径 /login', 'TEXT', '', '', 'config-class-oauth2', NOW(), 'system', NOW(), 'system')
ON DUPLICATE KEY UPDATE `config_value` = VALUES(`config_value`), `config_default` = VALUES(`config_default`),
                        `class_id` = VALUES(`class_id`);