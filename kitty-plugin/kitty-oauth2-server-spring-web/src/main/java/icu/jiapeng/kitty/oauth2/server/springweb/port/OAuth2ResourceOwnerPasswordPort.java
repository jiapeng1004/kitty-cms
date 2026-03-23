package icu.jiapeng.kitty.oauth2.server.springweb.port;

import java.util.Optional;

/**
 * 密码模式资源所有者认证（外围：本地用户、LDAP、IdP 等）。
 */
public interface OAuth2ResourceOwnerPasswordPort {

    /**
     * @return 资源所有者主体 id（如用户 id），失败返回 empty
     */
    Optional<String> authenticate(String username, String password);
}
