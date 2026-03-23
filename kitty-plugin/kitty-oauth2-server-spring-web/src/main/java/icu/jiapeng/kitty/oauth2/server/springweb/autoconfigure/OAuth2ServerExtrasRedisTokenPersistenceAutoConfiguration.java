package icu.jiapeng.kitty.oauth2.server.springweb.autoconfigure;

import icu.jiapeng.kitty.oauth2.server.springweb.extras.redis.RedisOAuth2AccessTokenPersistenceAdapter;
import icu.jiapeng.kitty.oauth2.server.springweb.extras.redis.RedisOAuth2AuthorizationCodePersistenceAdapter;
import icu.jiapeng.kitty.oauth2.server.springweb.extras.redis.RedisOAuth2RefreshTokenPersistenceAdapter;
import icu.jiapeng.kitty.oauth2.server.springweb.port.OAuth2AccessTokenPersistencePort;
import icu.jiapeng.kitty.oauth2.server.springweb.port.OAuth2AuthorizationCodePersistencePort;
import icu.jiapeng.kitty.oauth2.server.springweb.port.OAuth2RefreshTokenPersistencePort;
import icu.jiapeng.kitty.oauth2.server.springweb.properties.OAuth2ExtrasPropertyPrefix;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import tools.jackson.databind.ObjectMapper;

/**
 * Redis：按分项（{@code access-token-persistence} 等，未设置则继承 {@code token-persistence}）注册三个持久化端口。
 */
@AutoConfiguration
public class OAuth2ServerExtrasRedisTokenPersistenceAutoConfiguration {

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(name = "org.springframework.data.redis.core.StringRedisTemplate")
    @ConditionalOnBean(StringRedisTemplate.class)
    static class RedisExplicit {

        @Bean
        @ConditionalOnMissingBean(OAuth2AuthorizationCodePersistencePort.class)
        @ConditionalOnExpression(OAuth2ExtrasPropertyPrefix.AUTHORIZATION_CODE_EFFECTIVE_EQ_REDIS)
        OAuth2AuthorizationCodePersistencePort oauth2AuthorizationCodePersistencePortRedisExplicit(
                StringRedisTemplate stringRedisTemplate,
                ObjectMapper objectMapper) {
            return new RedisOAuth2AuthorizationCodePersistenceAdapter(stringRedisTemplate, objectMapper);
        }

        @Bean
        @ConditionalOnMissingBean(OAuth2AccessTokenPersistencePort.class)
        @ConditionalOnExpression(OAuth2ExtrasPropertyPrefix.ACCESS_TOKEN_EFFECTIVE_EQ_REDIS)
        OAuth2AccessTokenPersistencePort oauth2AccessTokenPersistencePortRedisExplicit(
                StringRedisTemplate stringRedisTemplate,
                ObjectMapper objectMapper) {
            return new RedisOAuth2AccessTokenPersistenceAdapter(stringRedisTemplate, objectMapper);
        }

        @Bean
        @ConditionalOnMissingBean(OAuth2RefreshTokenPersistencePort.class)
        @ConditionalOnExpression(OAuth2ExtrasPropertyPrefix.REFRESH_TOKEN_EFFECTIVE_EQ_REDIS)
        OAuth2RefreshTokenPersistencePort oauth2RefreshTokenPersistencePortRedisExplicit(
                StringRedisTemplate stringRedisTemplate,
                ObjectMapper objectMapper) {
            return new RedisOAuth2RefreshTokenPersistenceAdapter(stringRedisTemplate, objectMapper);
        }
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(name = "org.springframework.data.redis.core.StringRedisTemplate")
    @ConditionalOnBean(StringRedisTemplate.class)
    static class RedisAuto {

        @Bean
        @ConditionalOnMissingBean(OAuth2AuthorizationCodePersistencePort.class)
        @ConditionalOnExpression(OAuth2ExtrasPropertyPrefix.AUTHORIZATION_CODE_EFFECTIVE_EQ_AUTO)
        OAuth2AuthorizationCodePersistencePort oauth2AuthorizationCodePersistencePortRedisAuto(
                StringRedisTemplate stringRedisTemplate,
                ObjectMapper objectMapper) {
            return new RedisOAuth2AuthorizationCodePersistenceAdapter(stringRedisTemplate, objectMapper);
        }

        @Bean
        @ConditionalOnMissingBean(OAuth2AccessTokenPersistencePort.class)
        @ConditionalOnExpression(OAuth2ExtrasPropertyPrefix.ACCESS_TOKEN_EFFECTIVE_EQ_AUTO)
        OAuth2AccessTokenPersistencePort oauth2AccessTokenPersistencePortRedisAuto(
                StringRedisTemplate stringRedisTemplate,
                ObjectMapper objectMapper) {
            return new RedisOAuth2AccessTokenPersistenceAdapter(stringRedisTemplate, objectMapper);
        }

        @Bean
        @ConditionalOnMissingBean(OAuth2RefreshTokenPersistencePort.class)
        @ConditionalOnExpression(OAuth2ExtrasPropertyPrefix.REFRESH_TOKEN_EFFECTIVE_EQ_AUTO)
        OAuth2RefreshTokenPersistencePort oauth2RefreshTokenPersistencePortRedisAuto(
                StringRedisTemplate stringRedisTemplate,
                ObjectMapper objectMapper) {
            return new RedisOAuth2RefreshTokenPersistenceAdapter(stringRedisTemplate, objectMapper);
        }
    }
}
