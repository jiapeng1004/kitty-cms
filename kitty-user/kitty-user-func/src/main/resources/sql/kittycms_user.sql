DROP TABLE IF EXISTS kt_user_aksk_client;
CREATE TABLE `kt_user_aksk_client` (
                                       `id` VARCHAR(255)  PRIMARY KEY ,
                                       `client_name`  VARCHAR(255)  NULL DEFAULT NULL ,
                                       `access_key`  VARCHAR(255)  NULL DEFAULT NULL ,
                                       `secret_key`  VARCHAR(255)  NULL DEFAULT NULL ,
                                       `allowed_scopes`  VARCHAR(255)  NULL DEFAULT NULL ,
                                       `status`  INT  NULL DEFAULT NULL ,
                                       `create_time`  TIMESTAMP  NULL DEFAULT NULL ,
                                       `creator`  VARCHAR(255)  NULL DEFAULT NULL ,
                                       `update_time`  TIMESTAMP  NULL DEFAULT NULL ,
                                       `updater`  VARCHAR(255)  NULL DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

DROP TABLE IF EXISTS kt_user_role_user;
CREATE TABLE `kt_user_role_user` (
                                     `id` VARCHAR(255)  PRIMARY KEY ,
                                     `role_id`  VARCHAR(255)  NULL DEFAULT NULL ,
                                     `user_id`  VARCHAR(255)  NULL DEFAULT NULL ,
                                     `create_time`  TIMESTAMP  NULL DEFAULT NULL ,
                                     `creator`  VARCHAR(255)  NULL DEFAULT NULL ,
                                     `update_time`  TIMESTAMP  NULL DEFAULT NULL ,
                                     `updater`  VARCHAR(255)  NULL DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

DROP TABLE IF EXISTS kt_user_oauth2_client;
CREATE TABLE `kt_user_oauth2_client` (
                                         `id` VARCHAR(255)  PRIMARY KEY ,
                                         `client_name`  VARCHAR(255)  NULL DEFAULT NULL ,
                                         `client_id`  VARCHAR(255)  NULL DEFAULT NULL ,
                                         `client_secret`  VARCHAR(255)  NULL DEFAULT NULL ,
                                         `allowed_scopes`  VARCHAR(255)  NULL DEFAULT NULL ,
                                         `allowed_grant_types`  VARCHAR(255)  NULL DEFAULT NULL ,
                                         `allow_authentication_methods`  VARCHAR(255)  NULL DEFAULT NULL ,
                                         `allowed_redirect_uris`  VARCHAR(255)  NULL DEFAULT NULL ,
                                         `access_token_timeout`  BIGINT  NULL DEFAULT NULL ,
                                         `refresh_token_timeout`  BIGINT  NULL DEFAULT NULL ,
                                         `black_list_exemption`  VARCHAR(255)  NULL DEFAULT NULL ,
                                         `status`  INT  NULL DEFAULT NULL ,
                                         `require_authorization_consent`  INT  NULL DEFAULT NULL ,
                                         `create_time`  TIMESTAMP  NULL DEFAULT NULL ,
                                         `creator`  VARCHAR(255)  NULL DEFAULT NULL ,
                                         `update_time`  TIMESTAMP  NULL DEFAULT NULL ,
                                         `updater`  VARCHAR(255)  NULL DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

DROP TABLE IF EXISTS kt_config_class;
CREATE TABLE `kt_config_class` (
                                   `id` VARCHAR(255)  PRIMARY KEY ,
                                   `class_name`  VARCHAR(255)  NULL DEFAULT NULL ,
                                   `class_desc`  VARCHAR(255)  NULL DEFAULT NULL ,
                                   `owner`  VARCHAR(255)  NULL DEFAULT NULL ,
                                   `create_time`  TIMESTAMP  NULL DEFAULT NULL ,
                                   `creator`  VARCHAR(255)  NULL DEFAULT NULL ,
                                   `update_time`  TIMESTAMP  NULL DEFAULT NULL ,
                                   `updater`  VARCHAR(255)  NULL DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

DROP TABLE IF EXISTS kt_user_user_attr;
CREATE TABLE `kt_user_user_attr` (
                                     `id` VARCHAR(255)  PRIMARY KEY ,
                                     `user_id`  VARCHAR(255)  NULL DEFAULT NULL ,
                                     `attr_key`  VARCHAR(255)  NULL DEFAULT NULL ,
                                     `attr_value`  VARCHAR(255)  NULL DEFAULT NULL ,
                                     `create_time`  TIMESTAMP  NULL DEFAULT NULL ,
                                     `creator`  VARCHAR(255)  NULL DEFAULT NULL ,
                                     `update_time`  TIMESTAMP  NULL DEFAULT NULL ,
                                     `updater`  VARCHAR(255)  NULL DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

DROP TABLE IF EXISTS kt_user_group;
CREATE TABLE `kt_user_group` (
                                 `id` VARCHAR(255)  PRIMARY KEY ,
                                 `group_name`  VARCHAR(255)  NULL DEFAULT NULL ,
                                 `tenant_id`  VARCHAR(255)  NULL DEFAULT NULL ,
                                 `create_time`  TIMESTAMP  NULL DEFAULT NULL ,
                                 `creator`  VARCHAR(255)  NULL DEFAULT NULL ,
                                 `update_time`  TIMESTAMP  NULL DEFAULT NULL ,
                                 `updater`  VARCHAR(255)  NULL DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

DROP TABLE IF EXISTS kt_tenant;
CREATE TABLE `kt_tenant` (
                             `id` VARCHAR(255)  PRIMARY KEY ,
                             `name`  VARCHAR(255)  NULL DEFAULT NULL ,
                             `create_time`  TIMESTAMP  NULL DEFAULT NULL ,
                             `creator`  VARCHAR(255)  NULL DEFAULT NULL ,
                             `update_time`  TIMESTAMP  NULL DEFAULT NULL ,
                             `updater`  VARCHAR(255)  NULL DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

DROP TABLE IF EXISTS kt_user_role;
CREATE TABLE `kt_user_role` (
                                `id` VARCHAR(255)  PRIMARY KEY ,
                                `role_name`  VARCHAR(255)  NULL DEFAULT NULL ,
                                `create_time`  TIMESTAMP  NULL DEFAULT NULL ,
                                `creator`  VARCHAR(255)  NULL DEFAULT NULL ,
                                `update_time`  TIMESTAMP  NULL DEFAULT NULL ,
                                `updater`  VARCHAR(255)  NULL DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

DROP TABLE IF EXISTS kt_permission_owner;
CREATE TABLE `kt_permission_owner` (
                                       `id` VARCHAR(255)  PRIMARY KEY ,
                                       `owner_id`  VARCHAR(255)  NULL DEFAULT NULL ,
                                       `owner_type`  VARCHAR(255)  NULL DEFAULT NULL ,
                                       `p_code`  VARCHAR(255)  NULL DEFAULT NULL ,
                                       `create_time`  TIMESTAMP  NULL DEFAULT NULL ,
                                       `creator`  VARCHAR(255)  NULL DEFAULT NULL ,
                                       `update_time`  TIMESTAMP  NULL DEFAULT NULL ,
                                       `updater`  VARCHAR(255)  NULL DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

DROP TABLE IF EXISTS kt_user_user;
CREATE TABLE `kt_user_user` (
                                `id` VARCHAR(255)  PRIMARY KEY ,
                                `real_name`  VARCHAR(255)  NULL DEFAULT NULL ,
                                `nick_name`  VARCHAR(255)  NULL DEFAULT NULL ,
                                `pwd`  VARCHAR(255)  NULL DEFAULT NULL ,
                                `phone`  VARCHAR(255)  NULL DEFAULT NULL ,
                                `email`  VARCHAR(255)  NULL DEFAULT NULL ,
                                `id_card`  VARCHAR(255)  NULL DEFAULT NULL ,
                                `status`  INT  NULL DEFAULT NULL ,
                                `deleted`  INT  NULL DEFAULT NULL ,
                                `create_time`  TIMESTAMP  NULL DEFAULT NULL ,
                                `creator`  VARCHAR(255)  NULL DEFAULT NULL ,
                                `update_time`  TIMESTAMP  NULL DEFAULT NULL ,
                                `updater`  VARCHAR(255)  NULL DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

DROP TABLE IF EXISTS kt_permission;
CREATE TABLE `kt_permission` (
                                 `id` VARCHAR(255)  PRIMARY KEY ,
                                 `p_name`  VARCHAR(255)  NULL DEFAULT NULL ,
                                 `p_code`  VARCHAR(255)  NULL DEFAULT NULL ,
                                 `p_type`  VARCHAR(255)  NULL DEFAULT NULL ,
                                 `p_desc`  VARCHAR(255)  NULL DEFAULT NULL ,
                                 `create_time`  TIMESTAMP  NULL DEFAULT NULL ,
                                 `creator`  VARCHAR(255)  NULL DEFAULT NULL ,
                                 `update_time`  TIMESTAMP  NULL DEFAULT NULL ,
                                 `updater`  VARCHAR(255)  NULL DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

DROP TABLE IF EXISTS kt_config;
CREATE TABLE `kt_config` (
                             `id` VARCHAR(255)  PRIMARY KEY ,
                             `config_name`  VARCHAR(255)  NOT NULL,
                             `config_key`  VARCHAR(255)  NOT NULL,
                             `config_desc`  VARCHAR(255)  NULL DEFAULT NULL ,
                             `config_way`  VARCHAR(255)  NOT NULL,
                             `config_enum`  VARCHAR(255)  NULL DEFAULT NULL ,
                             `config_value`  VARCHAR(255)  NULL DEFAULT NULL ,
                             `config_default`  VARCHAR(255)  NULL DEFAULT NULL ,
                             `class_id`  VARCHAR(255)  NULL DEFAULT NULL ,
                             `create_time`  TIMESTAMP  NULL DEFAULT NULL ,
                             `creator`  VARCHAR(255)  NULL DEFAULT NULL ,
                             `update_time`  TIMESTAMP  NULL DEFAULT NULL ,
                             `updater`  VARCHAR(255)  NULL DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;