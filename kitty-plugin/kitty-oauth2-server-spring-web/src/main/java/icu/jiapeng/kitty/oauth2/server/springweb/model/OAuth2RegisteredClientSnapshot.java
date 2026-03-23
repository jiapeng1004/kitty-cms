package icu.jiapeng.kitty.oauth2.server.springweb.model;

import java.util.Set;

/**
 * OAuth2 客户端在授权服务器中的只读快照（由外围适配层从 DB/缓存映射而来）。
 *
 * @param requireAuthorizationConsent 为 {@code false} 时不展示 consent（仍受免责 scope 合并策略影响见实现）。
 * @param consentExemptScopes         客户端声明的免责 scope：若本次请求 scope 集合为其子集则可跳过 consent。
 */
public record OAuth2RegisteredClientSnapshot(
        String clientId,
        String clientSecret,
        Set<String> allowedGrantTypes,
        Set<String> allowedScopes,
        Set<String> allowedRedirectUris,
        Set<String> allowAuthenticationMethods,
        Long accessTokenTtlSeconds,
        Long refreshTokenTtlSeconds,
        boolean requireAuthorizationConsent,
        Set<String> consentExemptScopes
) {
    public OAuth2RegisteredClientSnapshot {
        allowedGrantTypes = allowedGrantTypes == null ? Set.of() : allowedGrantTypes;
        allowedScopes = allowedScopes == null ? Set.of() : allowedScopes;
        allowedRedirectUris = allowedRedirectUris == null ? Set.of() : allowedRedirectUris;
        allowAuthenticationMethods = allowAuthenticationMethods == null ? Set.of() : allowAuthenticationMethods;
        consentExemptScopes = consentExemptScopes == null ? Set.of() : consentExemptScopes;
    }
}
