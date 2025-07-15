-- 创建数据库
-- CREATE DATABASE IF NOT EXISTS kitty_cms charset utf8mb4 collate utf8mb4_general_ci;
-- USE kitty_cms;

-- 用户表
CREATE TABLE IF NOT EXISTS kt_user_user
(
    id          VARCHAR(32) PRIMARY KEY,
    real_name   VARCHAR(255) NOT NULL COMMENT '真实姓名',
    nick_name   VARCHAR(255) NULL     DEFAULT NULL COMMENT '昵称',
    pwd         VARCHAR(32)  NOT NULL COMMENT '密码',
    phone       VARCHAR(32)  NULL     DEFAULT NULL COMMENT '手机号',
    email       VARCHAR(32)  NULL     DEFAULT NULL COMMENT '邮箱',
    id_card     VARCHAR(32)  NULL     DEFAULT NULL COMMENT '身份证号',
    status      INT          NOT NULL COMMENT '状态',
    create_time TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP    NULL     DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    creator     VARCHAR(32)  NULL     DEFAULT NULL COMMENT '创建者',
    updater     VARCHAR(32)  NULL     DEFAULT NULL COMMENT '更新者',
    UNIQUE Key `idx_login_name` (`login_name`) USING BTREE COMMENT '登录名索引',
    Key `idx_phone` (`phone`) USING BTREE COMMENT '手机号索引',
    Key `idx_email` (`email`) USING BTREE COMMENT '邮箱索引',
    Key `idx_id_card` (`id_card`) USING BTREE COMMENT '身份证号索引',
    Key `idx_nick_name` (`nick_name`) USING BTREE COMMENT '昵称索引',
    Key `idx_real_name` (`real_name`) USING BTREE COMMENT '真实姓名索引'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci
  ROW_FORMAT = DYNAMIC COMMENT ='用户表';

-- 角色表
CREATE TABLE IF NOT EXISTS kt_user_role
(
    id          VARCHAR(32) PRIMARY KEY,
    role_name   VARCHAR(32) NOT NULL COMMENT '角色名',
    create_time TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP   NULL     DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    creator     VARCHAR(32) NULL     DEFAULT NULL COMMENT '创建者',
    updater     VARCHAR(32) NULL     DEFAULT NULL COMMENT '更新者'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci
  ROW_FORMAT = DYNAMIC COMMENT ='角色表';

-- 用户属性表
CREATE TABLE IF NOT EXISTS kt_user_user_attr
(
    id          VARCHAR(32) PRIMARY KEY,
    user_id     VARCHAR(32) NOT NULL COMMENT '用户id',
    attr_key    VARCHAR(64) NOT NULL COMMENT '属性key',
    attr_value  TEXT        NOT NULL COMMENT '属性值',
    create_time TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP   NULL     DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    creator     VARCHAR(32) NULL     DEFAULT NULL COMMENT '创建者',
    updater     VARCHAR(32) NULL     DEFAULT NULL COMMENT '更新者',
    UNIQUE KEY `idx_user_attr` (`user_id`, `attr_key`) USING BTREE COMMENT '用户属性唯一索引'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci
  ROW_FORMAT = DYNAMIC COMMENT ='用户属性表';

-- 组织表
CREATE TABLE IF NOT EXISTS kt_user_group
(
    id          VARCHAR(32) PRIMARY KEY,
    groupName   VARCHAR(64) NOT NULL COMMENT '组织名',
    create_time TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP   NULL     DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    creator     VARCHAR(32) NULL     DEFAULT NULL COMMENT '创建者',
    updater     VARCHAR(32) NULL     DEFAULT NULL COMMENT '更新者',
    UNIQUE KEY `idx_group_name` (`groupName`) USING BTREE COMMENT '组织名唯一索引'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci
  ROW_FORMAT = DYNAMIC COMMENT ='组织表';


-- oauth2 客户端
CREATE TABLE IF NOT EXISTS kt_user_oauth2_client
(
    id                    VARCHAR(32) PRIMARY KEY,
    client_id             VARCHAR(32)  NOT NULL COMMENT '客户端id',
    client_secret         VARCHAR(255)  NOT NULL COMMENT '客户端密钥',
    client_name           VARCHAR(255) NOT NULL COMMENT '客户端名称',
    allowed_scopes        TEXT         NOT NULL COMMENT '允许的scope',
    allowed_grant_types   TEXT         NOT NULL COMMENT '允许的授权类型',
    allow_authentication_methods   TEXT         NOT NULL COMMENT '允许的客户端授权方法',
    allowed_redirect_uris TEXT         NOT NULL COMMENT '允许的回调地址',
    access_token_timeout  BIGINT          NULL  DEFAULT NULL COMMENT 'access_token超时时间',
    refresh_token_timeout BIGINT          NULL  DEFAULT NULL COMMENT 'refresh_token超时时间',
    require_authorization_consent INT     NULL  DEFAULT NULL COMMENT '是否需要授权确认',
    black_list_exemption  VARCHAR(10) NULL DEFAULT NULL COMMENT '黑名单豁免flag,为1客户不过黑名单',
    status                INT          NOT NULL COMMENT '状态',
    create_time           TIMESTAMP    NULL DEFAULT NULL COMMENT '创建时间',
    creator               VARCHAR(32)  NULL DEFAULT NULL COMMENT '创建者',
    update_time           TIMESTAMP    NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    updater               VARCHAR(32)  NULL DEFAULT NULL COMMENT '更新者',
    UNIQUE KEY `idx_client_id` (`client_id`) USING BTREE COMMENT '客户端id索引'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci
  ROW_FORMAT = DYNAMIC COMMENT ='oauth2客户端表';