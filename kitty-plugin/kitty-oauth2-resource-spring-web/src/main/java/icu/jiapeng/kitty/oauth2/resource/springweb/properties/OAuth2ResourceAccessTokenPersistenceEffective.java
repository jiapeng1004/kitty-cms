package icu.jiapeng.kitty.oauth2.resource.springweb.properties;

import icu.jiapeng.kitty.oauth2.resource.springweb.autoconfigure.OAuth2ExtrasPropertyPrefix;
import org.springframework.core.env.Environment;

/**
 * 资源侧只校验 access_token：分项 {@code access-token-persistence} 未设置时回退到 {@code token-persistence}。
 */
public final class OAuth2ResourceAccessTokenPersistenceEffective {

    private static final String P = OAuth2ExtrasPropertyPrefix.EXTRAS + ".";

    private OAuth2ResourceAccessTokenPersistenceEffective() {}

    public static OAuth2ExtrasTokenPersistence effective(Environment env) {
        String explicit = env.getProperty(P + "access-token-persistence");
        if (explicit != null && !explicit.isBlank()) {
            return OAuth2ExtrasTokenPersistence.fromConfigurationValue(explicit);
        }
        return OAuth2ExtrasTokenPersistence.fromConfigurationValue(env.getProperty(P + "token-persistence", "auto"));
    }
}
