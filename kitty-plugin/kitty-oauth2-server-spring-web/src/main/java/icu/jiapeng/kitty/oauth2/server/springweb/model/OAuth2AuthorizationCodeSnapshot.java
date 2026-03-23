package icu.jiapeng.kitty.oauth2.server.springweb.model;

/**
 * 授权码载荷（与持久化序列化格式无关，由适配层负责落库/Redis）。
 */
public record OAuth2AuthorizationCodeSnapshot(
        String clientId,
        String userId,
        String redirectUri,
        String scope,
        String codeChallenge,
        String codeChallengeMethod
) {
}
