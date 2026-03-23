package icu.jiapeng.kitty.oauth2.server.springweb.port;

import icu.jiapeng.kitty.oauth2.server.springweb.model.OAuth2AuthorizationCodeSnapshot;

import java.util.Optional;

/**
 * 授权码（authorization code）持久化；与访问/刷新令牌存储解耦，便于分别替换或横向扩展。
 */
public interface OAuth2AuthorizationCodePersistencePort {

    void putAuthorizationCode(String code, OAuth2AuthorizationCodeSnapshot payload, int ttlSeconds);

    Optional<OAuth2AuthorizationCodeSnapshot> consumeAuthorizationCode(String code);
}
