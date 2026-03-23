package icu.jiapeng.kitty.oauth2.server.springweb.autoconfigure;

import icu.jiapeng.kitty.oauth2.server.springweb.properties.OAuth2ExtrasPropertyPrefix;
import icu.jiapeng.kitty.oauth2.server.springweb.extras.springsecurity.SpringSecurityOAuth2EndUserSessionAdapter;
import icu.jiapeng.kitty.oauth2.server.springweb.port.OAuth2EndUserSessionPort;
import icu.jiapeng.kitty.oauth2.server.springweb.properties.KittyOAuth2AuthorizationServerProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring Security 可选集成：提供 {@link OAuth2EndUserSessionPort} 默认实现。
 * <p>
 * 显式：{@code extras.end-user-session=spring-security}；自动：{@code havingValue = "auto"}。
 */
@AutoConfiguration
@EnableConfigurationProperties(KittyOAuth2AuthorizationServerProperties.class)
public class OAuth2ExtrasSpringSecurityAutoConfiguration {

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(name = "org.springframework.security.core.context.SecurityContextHolder")
    @ConditionalOnProperty(
            prefix = OAuth2ExtrasPropertyPrefix.EXTRAS,
            name = "end-user-session",
            havingValue = "spring-security")
    static class SpringSecurityOAuth2EndUserSessionExplicitConfiguration {

        @Bean
        @ConditionalOnMissingBean(OAuth2EndUserSessionPort.class)
        OAuth2EndUserSessionPort oauth2EndUserSessionPortSpringSecurityExplicit(
                KittyOAuth2AuthorizationServerProperties properties) {
            return new SpringSecurityOAuth2EndUserSessionAdapter(properties);
        }
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(name = "org.springframework.security.core.context.SecurityContextHolder")
    @ConditionalOnProperty(
            prefix = OAuth2ExtrasPropertyPrefix.EXTRAS,
            name = "end-user-session",
            havingValue = "auto")
    static class SpringSecurityOAuth2EndUserSessionAutoConfiguration {

        @Bean
        @ConditionalOnMissingBean(OAuth2EndUserSessionPort.class)
        OAuth2EndUserSessionPort oauth2EndUserSessionPortSpringSecurityAuto(
                KittyOAuth2AuthorizationServerProperties properties) {
            return new SpringSecurityOAuth2EndUserSessionAdapter(properties);
        }
    }
}
