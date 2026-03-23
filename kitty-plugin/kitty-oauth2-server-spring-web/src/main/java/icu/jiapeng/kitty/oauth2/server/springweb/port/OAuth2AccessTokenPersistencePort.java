package icu.jiapeng.kitty.oauth2.server.springweb.port;

import icu.jiapeng.kitty.oauth2.server.springweb.model.OAuth2TokenSnapshot;

import java.util.Optional;

/**
 * 访问令牌（access token）持久化；与授权码、刷新令牌存储解耦。
 */
public interface OAuth2AccessTokenPersistencePort {

    void putAccessToken(String token, OAuth2TokenSnapshot payload, int ttlSeconds);

    Optional<OAuth2TokenSnapshot> getAccessToken(String accessToken);

    /**
     * 删除已颁发的 access_token（RFC 7009 撤销等）；与 {@link #putAccessToken} 使用相同键/行定位。
     */
    void removeAccessToken(String accessToken);
}
