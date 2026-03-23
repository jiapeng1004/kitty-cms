package icu.jiapeng.kitty.oauth2.server.springweb.port;

import icu.jiapeng.kitty.oauth2.server.springweb.model.OAuth2TokenSnapshot;

import java.util.Optional;

/**
 * 刷新令牌（refresh token）持久化；与授权码、访问令牌存储解耦。
 */
public interface OAuth2RefreshTokenPersistencePort {

    void putRefreshToken(String token, OAuth2TokenSnapshot payload, int ttlSeconds);

    Optional<OAuth2TokenSnapshot> getRefreshToken(String refreshToken);

    void removeRefreshToken(String refreshToken);
}
