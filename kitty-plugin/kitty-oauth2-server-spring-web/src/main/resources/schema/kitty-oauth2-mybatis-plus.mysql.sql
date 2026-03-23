-- OAuth2 MyBatis-Plus：用户-客户端 consent 表（授权服务器）。
-- 授权码 / access_token / refresh_token 表见依赖模块 classpath:schema/kt-oauth2-token-tables.mysql.sql（kitty-oauth2-resource-spring-web）。

CREATE TABLE IF NOT EXISTS `kt_oauth2_user_client_consent` (
    `id`         BIGINT       NOT NULL AUTO_INCREMENT,
    `user_id`    VARCHAR(191) NOT NULL,
    `client_id`  VARCHAR(191) NOT NULL,
    `scopes`     TEXT         NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_oauth2_consent_user_client` (`user_id`, `client_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
