package icu.jiapeng.kitty.oauth2.server.springweb.internal;

import icu.jiapeng.kitty.oauth2.server.springweb.port.OAuth2ConsentStoragePort;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 进程内记录用户-客户端已同意 scope（生产请启用 {@code extras.consent-storage=auto} 与 MyBatis-Plus 集成，或自建 {@link icu.jiapeng.kitty.oauth2.server.springweb.port.OAuth2ConsentStoragePort}）。
 */
public class InMemoryOAuth2ConsentStorageAdapter implements OAuth2ConsentStoragePort {

    private final Map<String, Set<String>> approved = new ConcurrentHashMap<>();

    private static String key(String userId, String clientId) {
        return userId + "\0" + clientId;
    }

    @Override
    public boolean hasConsented(String userId, String clientId, Set<String> requestedScopes) {
        if (requestedScopes == null || requestedScopes.isEmpty()) {
            return true;
        }
        Set<String> have = approved.get(key(userId, clientId));
        if (have == null || have.isEmpty()) {
            return false;
        }
        return have.containsAll(requestedScopes);
    }

    @Override
    public void recordConsent(String userId, String clientId, Set<String> grantedScopes) {
        if (grantedScopes == null || grantedScopes.isEmpty()) {
            return;
        }
        approved.compute(key(userId, clientId), (k, v) -> {
            Set<String> n = v == null ? new HashSet<>() : new HashSet<>(v);
            n.addAll(grantedScopes);
            return n;
        });
    }
}
