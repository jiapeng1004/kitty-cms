package icu.jiapeng.kitty.oauth2.server.springweb.autoconfigure;

import icu.jiapeng.kitty.oauth2.server.springweb.extras.mybatisplus.MybatisPlusOAuth2AccessTokenPersistenceAdapter;
import icu.jiapeng.kitty.oauth2.server.springweb.extras.mybatisplus.MybatisPlusOAuth2AuthorizationCodePersistenceAdapter;
import icu.jiapeng.kitty.oauth2.server.springweb.extras.mybatisplus.MybatisPlusOAuth2RefreshTokenPersistenceAdapter;
import icu.jiapeng.kitty.oauth2.server.springweb.extras.mybatisplus.mapper.OAuth2AccessTokenMapper;
import icu.jiapeng.kitty.oauth2.server.springweb.extras.mybatisplus.mapper.OAuth2AuthorizationCodeMapper;
import icu.jiapeng.kitty.oauth2.server.springweb.extras.mybatisplus.mapper.OAuth2RefreshTokenMapper;
import icu.jiapeng.kitty.oauth2.server.springweb.port.OAuth2AccessTokenPersistencePort;
import icu.jiapeng.kitty.oauth2.server.springweb.port.OAuth2AuthorizationCodePersistencePort;
import icu.jiapeng.kitty.oauth2.server.springweb.port.OAuth2RefreshTokenPersistencePort;
import icu.jiapeng.kitty.oauth2.server.springweb.properties.OAuth2ExtrasPropertyPrefix;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.ObjectMapper;

import javax.sql.DataSource;

/**
 * MyBatis-Plus：分项与 {@code token-persistence} 回退一致；与 Redis 通过各端口上 {@link ConditionalOnMissingBean} 择一生效。
 * <p>
 * {@link MapperScan} 放在本类（仅要求 {@link DataSource}），与 {@link OAuth2ExtrasMybatisPlusAutoConfiguration} 共用
 * {@code ...extras.mybatisplus.mapper} 包，避免两套条件类再决定是否扫描。
 */
@AutoConfiguration
@ConditionalOnClass(name = "com.baomidou.mybatisplus.core.mapper.BaseMapper")
public class OAuth2ServerExtrasMybatisPlusTokenPersistenceAutoConfiguration {

    @Configuration(proxyBeanMethods = false)
    @MapperScan("icu.jiapeng.kitty.oauth2.server.springweb.extras.mybatisplus.mapper")
    static class OAuth2TokenPersistenceMybatisPlusBeans {

        @Bean
        @ConditionalOnMissingBean(OAuth2AuthorizationCodePersistencePort.class)
        @ConditionalOnExpression(OAuth2ExtrasPropertyPrefix.AUTHORIZATION_CODE_EFFECTIVE_AUTO_OR_MYBATIS_PLUS)
        OAuth2AuthorizationCodePersistencePort oauth2AuthorizationCodePersistencePortMybatisPlus(
                OAuth2AuthorizationCodeMapper authorizationCodeMapper,
                ObjectMapper objectMapper) {
            return new MybatisPlusOAuth2AuthorizationCodePersistenceAdapter(authorizationCodeMapper, objectMapper);
        }

        @Bean
        @ConditionalOnMissingBean(OAuth2AccessTokenPersistencePort.class)
        @ConditionalOnExpression(OAuth2ExtrasPropertyPrefix.ACCESS_TOKEN_EFFECTIVE_AUTO_OR_MYBATIS_PLUS)
        OAuth2AccessTokenPersistencePort oauth2AccessTokenPersistencePortMybatisPlus(
                OAuth2AccessTokenMapper accessTokenMapper,
                ObjectMapper objectMapper) {
            return new MybatisPlusOAuth2AccessTokenPersistenceAdapter(accessTokenMapper, objectMapper);
        }

        @Bean
        @ConditionalOnMissingBean(OAuth2RefreshTokenPersistencePort.class)
        @ConditionalOnExpression(OAuth2ExtrasPropertyPrefix.REFRESH_TOKEN_EFFECTIVE_AUTO_OR_MYBATIS_PLUS)
        OAuth2RefreshTokenPersistencePort oauth2RefreshTokenPersistencePortMybatisPlus(
                OAuth2RefreshTokenMapper refreshTokenMapper,
                ObjectMapper objectMapper) {
            return new MybatisPlusOAuth2RefreshTokenPersistenceAdapter(refreshTokenMapper, objectMapper);
        }
    }
}
