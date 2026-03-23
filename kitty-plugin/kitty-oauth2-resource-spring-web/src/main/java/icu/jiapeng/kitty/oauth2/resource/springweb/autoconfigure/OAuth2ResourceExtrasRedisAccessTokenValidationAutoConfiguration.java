package icu.jiapeng.kitty.oauth2.resource.springweb.autoconfigure;

import icu.jiapeng.kitty.oauth2.resource.springweb.extras.redis.RedisOAuth2AccessTokenValidationAdapter;
import icu.jiapeng.kitty.oauth2.resource.springweb.port.OAuth2AccessTokenValidationPort;
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
 * Redis：按 access 分项（或回退的 {@code token-persistence}）注册 {@link OAuth2AccessTokenValidationPort}（只读）。
 */
@AutoConfiguration
public class OAuth2ResourceExtrasRedisAccessTokenValidationAutoConfiguration {

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(name = "org.springframework.data.redis.core.StringRedisTemplate")
    @ConditionalOnBean(StringRedisTemplate.class)
    static class RedisExplicit {

        @Bean
        @ConditionalOnMissingBean(OAuth2AccessTokenValidationPort.class)
        @ConditionalOnExpression(OAuth2ResourceAccessTokenPersistenceConditionalExpressions.EFFECTIVE_EQ_REDIS)
        OAuth2AccessTokenValidationPort oauth2AccessTokenValidationPortRedisExplicit(
                StringRedisTemplate stringRedisTemplate,
                ObjectMapper objectMapper) {
            return new RedisOAuth2AccessTokenValidationAdapter(stringRedisTemplate, objectMapper);
        }
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(name = "org.springframework.data.redis.core.StringRedisTemplate")
    @ConditionalOnBean(StringRedisTemplate.class)
    static class RedisAuto {

        @Bean
        @ConditionalOnMissingBean(OAuth2AccessTokenValidationPort.class)
        @ConditionalOnExpression(OAuth2ResourceAccessTokenPersistenceConditionalExpressions.EFFECTIVE_EQ_AUTO)
        OAuth2AccessTokenValidationPort oauth2AccessTokenValidationPortRedisAuto(
                StringRedisTemplate stringRedisTemplate,
                ObjectMapper objectMapper) {
            return new RedisOAuth2AccessTokenValidationAdapter(stringRedisTemplate, objectMapper);
        }
    }
}
