package icu.jiapeng.kitty.oauth2.server.springweb.port;

import java.util.Set;

/**
 * 用户对某客户端已同意的 scope 集合（可持久化到 DB/Redis；默认可有内存实现）。
 */
public interface OAuth2ConsentStoragePort {

    /**
     * 是否已覆盖本次请求所需 scope（已同意集合包含全部 requested）。
     */
    boolean hasConsented(String userId, String clientId, Set<String> requestedScopes);

    /**
     * 记录用户同意（与既有已同意 scope 合并）。
     */
    void recordConsent(String userId, String clientId, Set<String> grantedScopes);
}
