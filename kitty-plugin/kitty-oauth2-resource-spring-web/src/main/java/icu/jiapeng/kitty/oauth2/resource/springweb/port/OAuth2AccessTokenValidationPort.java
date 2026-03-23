package icu.jiapeng.kitty.oauth2.resource.springweb.port;

import icu.jiapeng.kitty.oauth2.resource.springweb.model.OAuth2TokenSnapshot;

import java.util.Optional;

/**
 * 资源侧仅<strong>解析 / 校验</strong> access_token（Bearer），不暴露授权服务器的写入能力。
 * <p>
 * 与授权服务器 {@code OAuth2AccessTokenPersistencePort}（定义于 {@code kitty-oauth2-server-spring-web}）通过 Redis 键、表结构等<strong>约定</strong>对齐，模块间无 Maven 依赖。
 */
public interface OAuth2AccessTokenValidationPort {

    /**
     * 按 access_token 原文解析载荷；无效则 empty。
     */
    Optional<OAuth2TokenSnapshot> resolveAccessToken(String rawAccessToken);
}
