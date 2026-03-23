package icu.jiapeng.kitty.user.oauth2.adapter.server;

import icu.jiapeng.kitty.oauth2.server.springweb.model.OAuth2RegisteredClientSnapshot;
import icu.jiapeng.kitty.oauth2.server.springweb.port.OAuth2RegisteredClientRegistryPort;
import icu.jiapeng.kitty.user.oauth2.entity.KtOauth2Client;
import icu.jiapeng.kitty.user.oauth2.service.KtOauth2ClientService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

/**
 * 从 {@link KtOauth2ClientService} 加载客户端，映射为插件侧快照。
 */
@Component
public class KtOauth2RegisteredClientRegistryAdapter implements OAuth2RegisteredClientRegistryPort {

    @Resource
    private KtOauth2ClientService ktOauth2ClientService;

    @Override
    public Optional<OAuth2RegisteredClientSnapshot> findEnabledByClientId(String clientId) {
        KtOauth2Client c = ktOauth2ClientService.getClientByClientIdWithCache(clientId);
        if (c == null || c.getStatus() == null || c.getStatus() != 1) {
            return Optional.empty();
        }
        return Optional.of(toSnapshot(c));
    }

    private static OAuth2RegisteredClientSnapshot toSnapshot(KtOauth2Client c) {
        boolean requireConsent = c.getRequireAuthorizationConsent() != null && c.getRequireAuthorizationConsent() == 1;
        return new OAuth2RegisteredClientSnapshot(
                c.getClientId(),
                c.getClientSecret(),
                splitComma(c.getAllowedGrantTypes()),
                splitComma(c.getAllowedScopes()),
                splitComma(c.getAllowedRedirectUris()),
                splitComma(c.getAllowAuthenticationMethods()),
                c.getAccessTokenTimeout(),
                c.getRefreshTokenTimeout(),
                requireConsent,
                Set.of());
    }

    private static Set<String> splitComma(String raw) {
        if (raw == null || raw.isBlank()) {
            return Set.of();
        }
        LinkedHashSet<String> set = new LinkedHashSet<>();
        for (String p : raw.split(",")) {
            String t = p.trim();
            if (!t.isEmpty()) {
                set.add(t);
            }
        }
        return set;
    }
}
