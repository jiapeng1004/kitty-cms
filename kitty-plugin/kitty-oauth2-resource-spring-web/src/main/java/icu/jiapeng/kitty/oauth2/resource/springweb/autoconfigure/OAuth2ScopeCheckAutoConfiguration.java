package icu.jiapeng.kitty.oauth2.resource.springweb.autoconfigure;

import icu.jiapeng.kitty.oauth2.resource.springweb.port.OAuth2AccessTokenValidationPort;
import icu.jiapeng.kitty.oauth2.resource.springweb.properties.OAuth2ScopeCheckProperties;
import icu.jiapeng.kitty.oauth2.resource.springweb.security.OAuth2CheckScopeHandlerInterceptor;
import icu.jiapeng.kitty.oauth2.resource.springweb.security.OAuth2ScopeCheck;
import icu.jiapeng.kitty.oauth2.resource.springweb.security.OAuth2ScopeCheckExceptionAdvice;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 注册 {@link OAuth2ScopeCheck}、{@link icu.jiapeng.kitty.oauth2.resource.springweb.security.CheckScope} 拦截器及异常响应。
 */
@AutoConfiguration
@ConditionalOnBean(OAuth2AccessTokenValidationPort.class)
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(OAuth2ScopeCheckProperties.class)
public class OAuth2ScopeCheckAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(OAuth2ScopeCheck.class)
    OAuth2ScopeCheck oauth2ScopeCheck(
            OAuth2AccessTokenValidationPort accessTokenValidation,
            OAuth2ScopeCheckProperties scopeCheckProperties) {
        return new OAuth2ScopeCheck(accessTokenValidation, scopeCheckProperties);
    }

    @Bean
    @ConditionalOnBean(OAuth2ScopeCheck.class)
    OAuth2ScopeCheckExceptionAdvice oauth2ScopeCheckExceptionAdvice() {
        return new OAuth2ScopeCheckExceptionAdvice();
    }

    @Bean
    @ConditionalOnBean(OAuth2ScopeCheck.class)
    @ConditionalOnProperty(
            prefix = "kitty.oauth2.authorization-server.scope-check",
            name = "enabled",
            havingValue = "true",
            matchIfMissing = true)
    WebMvcConfigurer oauth2CheckScopeWebMvcConfigurer(OAuth2ScopeCheck scopeCheck) {
        return new WebMvcConfigurer() {
            @Override
            public void addInterceptors(@NonNull InterceptorRegistry registry) {
                registry.addInterceptor(new OAuth2CheckScopeHandlerInterceptor(scopeCheck))
                        .addPathPatterns("/**")
                        .order(Ordered.LOWEST_PRECEDENCE);
            }
        };
    }
}
