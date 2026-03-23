package icu.jiapeng.kitty.oauth2.server.springweb.port;

import icu.jiapeng.kitty.oauth2.server.springweb.model.OAuth2RegisteredClientSnapshot;

import java.util.Optional;

/**
 * 已启用 OAuth2 客户端查询（外围：DB、缓存、远程注册中心等）。
 */
public interface OAuth2RegisteredClientRegistryPort {

    Optional<OAuth2RegisteredClientSnapshot> findEnabledByClientId(String clientId);
}
