package icu.jiapeng.kitty.oauth2.resource.springweb.env;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;

/**
 * 当 classpath 上<strong>没有</strong>授权服务器 starter 且用户未显式配置
 * {@code access-token-persistence} 与 {@code token-persistence} 时，将默认值
 * {@code access-token-persistence=center}（中心化 RFC 7662 自省），避免影响授权码 / refresh 分项语义。
 * <p>
 * 与 AS 同进程时（存在 {@code OAuth2AuthorizationServerSpringWebAutoConfiguration}）不注入，以免覆盖宿主对授权服务器默认 {@code auto} 的意图。
 */
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class OAuth2ResourceTokenPersistenceDefaultEnvironmentPostProcessor implements EnvironmentPostProcessor {

    static final String ACCESS_TOKEN_PERSISTENCE_KEY = "kitty.oauth2.authorization-server.extras.access-token-persistence";

    static final String TOKEN_PERSISTENCE_KEY = "kitty.oauth2.authorization-server.extras.token-persistence";

    private static final String SERVER_AUTOCONFIG =
            "icu.jiapeng.kitty.oauth2.server.springweb.autoconfigure.OAuth2AuthorizationServerSpringWebAutoConfiguration";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        if (environment.getProperty(ACCESS_TOKEN_PERSISTENCE_KEY) != null) {
            return;
        }
        if (environment.getProperty(TOKEN_PERSISTENCE_KEY) != null) {
            return;
        }
        if (isServerStarterOnClasspath()) {
            return;
        }
        Map<String, Object> map = new HashMap<>();
        map.put(ACCESS_TOKEN_PERSISTENCE_KEY, "center");
        environment.getPropertySources().addLast(new MapPropertySource("kittyOAuth2ResourceTokenPersistenceDefaults", map));
    }

    private static boolean isServerStarterOnClasspath() {
        try {
            Class.forName(SERVER_AUTOCONFIG, false, OAuth2ResourceTokenPersistenceDefaultEnvironmentPostProcessor.class.getClassLoader());
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
