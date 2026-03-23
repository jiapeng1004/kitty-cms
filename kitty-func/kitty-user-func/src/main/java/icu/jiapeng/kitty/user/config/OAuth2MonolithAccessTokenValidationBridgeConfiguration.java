package icu.jiapeng.kitty.user.config;

import icu.jiapeng.kitty.oauth2.resource.springweb.model.OAuth2TokenSnapshot;
import icu.jiapeng.kitty.oauth2.resource.springweb.port.OAuth2AccessTokenValidationPort;
import icu.jiapeng.kitty.oauth2.server.springweb.port.OAuth2AccessTokenPersistencePort;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 授权服务器与资源侧同进程时，将 {@link OAuth2AccessTokenPersistencePort} 桥接为 {@link OAuth2AccessTokenValidationPort}，
 * 避免资源 extras 再注册一套 Redis/MyBatis 只读 Bean（可选；若不需要可删除本配置，由资源模块按 {@code extras.access-token-persistence} / {@code extras.token-persistence} 自行注册校验端口）。
 */
@Configuration
@ConditionalOnBean(OAuth2AccessTokenPersistencePort.class)
public class OAuth2MonolithAccessTokenValidationBridgeConfiguration {

    @Bean
    @ConditionalOnMissingBean(OAuth2AccessTokenValidationPort.class)
    OAuth2AccessTokenValidationPort oauth2AccessTokenValidationPortFromPersistence(
            OAuth2AccessTokenPersistencePort accessTokenPersistence) {
        return raw ->
                accessTokenPersistence
                        .getAccessToken(raw)
                        .map(s -> new OAuth2TokenSnapshot(s.clientId(), s.subject(), s.scope()));
    }
}
