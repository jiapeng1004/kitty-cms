-- OAuth2 用户在某客户端下已同意的 scope（空格分隔），供 MybatisPlusOAuth2ConsentStorageAdapter 使用。
-- 完整建表（含授权码 / access / refresh）请使用同目录 kitty-oauth2-mybatis-plus.mysql.sql。
CREATE TABLE IF NOT EXISTS `kt_oauth2_user_client_consent` (
    `id`         BIGINT       NOT NULL AUTO_INCREMENT,
    `user_id`    VARCHAR(191) NOT NULL,
    `client_id`  VARCHAR(191) NOT NULL,
    `scopes`     TEXT         NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_oauth2_consent_user_client` (`user_id`, `client_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
