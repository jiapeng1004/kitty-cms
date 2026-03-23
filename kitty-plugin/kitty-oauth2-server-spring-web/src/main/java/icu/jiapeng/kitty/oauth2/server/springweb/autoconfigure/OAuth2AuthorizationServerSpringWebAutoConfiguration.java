package icu.jiapeng.kitty.oauth2.server.springweb.autoconfigure;

import icu.jiapeng.kitty.oauth2.server.springweb.config.OAuth2AuthorizationServerSpringWebConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Import;

/**
 * Spring Boot 自动配置入口（starter）：导入 {@link OAuth2AuthorizationServerSpringWebConfiguration}。
 * <p>
 * 注册方式：{@code META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports} 与
 * 兼容用 {@code META-INF/spring.factories}。宿主仍可通过 {@code @Import(OAuth2AuthorizationServerSpringWebConfiguration.class)} 显式装配（与自动配置二选一即可，避免重复 Import）。
 */
@AutoConfiguration(after = {
        OAuth2ExtrasSaTokenAutoConfiguration.class,
        OAuth2ExtrasSpringSecurityAutoConfiguration.class,
        OAuth2ServerExtrasRedisTokenPersistenceAutoConfiguration.class,
        OAuth2ServerExtrasMybatisPlusTokenPersistenceAutoConfiguration.class,
        OAuth2ExtrasMybatisPlusAutoConfiguration.class
})
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@Import(OAuth2AuthorizationServerSpringWebConfiguration.class)
public class OAuth2AuthorizationServerSpringWebAutoConfiguration {
}
