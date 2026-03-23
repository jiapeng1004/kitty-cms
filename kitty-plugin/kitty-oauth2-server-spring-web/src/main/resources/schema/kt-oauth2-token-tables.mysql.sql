-- OAuth2 令牌持久化（MyBatis-Plus，与 kitty-oauth2-server-spring-web extras 可选实现一致）。
-- expires_at：UTC；access_token / refresh_token 主键为原始令牌的 SHA-256 十六进制（64 字符）。

CREATE TABLE IF NOT EXISTS `kt_oauth2_authorization_code` (
    `code`         VARCHAR(512)  NOT NULL,
    `payload_json` MEDIUMTEXT      NOT NULL,
    `expires_at`   DATETIME(6)     NOT NULL,
    PRIMARY KEY (`code`),
    KEY `idx_oauth2_auth_code_expires` (`expires_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `kt_oauth2_access_token` (
    `token_hash`   CHAR(64)     NOT NULL COMMENT 'SHA-256 hex of raw access token',
    `payload_json` MEDIUMTEXT   NOT NULL,
    `expires_at`   DATETIME(6)  NOT NULL,
    PRIMARY KEY (`token_hash`),
    KEY `idx_oauth2_at_expires` (`expires_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `kt_oauth2_refresh_token` (
    `token_hash`   CHAR(64)     NOT NULL COMMENT 'SHA-256 hex of raw refresh token',
    `payload_json` MEDIUMTEXT   NOT NULL,
    `expires_at`   DATETIME(6)  NOT NULL,
    PRIMARY KEY (`token_hash`),
    KEY `idx_oauth2_rt_expires` (`expires_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
