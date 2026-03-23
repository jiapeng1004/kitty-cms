package icu.jiapeng.kitty.oauth2.server.springweb.config;

import icu.jiapeng.kitty.oauth2.server.springweb.api.OAuth2AuthorizationServerService;
import icu.jiapeng.kitty.oauth2.server.springweb.internal.OAuth2AuthorizationServerCoreService;
import icu.jiapeng.kitty.oauth2.server.springweb.internal.DefaultOAuth2ConsentUiAdapter;
import icu.jiapeng.kitty.oauth2.server.springweb.internal.InMemoryOAuth2ConsentStorageAdapter;
import icu.jiapeng.kitty.oauth2.server.springweb.port.OAuth2AuthorizationServerPropertiesPort;
import icu.jiapeng.kitty.oauth2.server.springweb.port.OAuth2ConsentStoragePort;
import icu.jiapeng.kitty.oauth2.server.springweb.port.OAuth2ConsentUiPort;
import icu.jiapeng.kitty.oauth2.server.springweb.port.OAuth2EndUserSessionPort;
import icu.jiapeng.kitty.oauth2.server.springweb.port.OAuth2LoginNavigationPort;
import icu.jiapeng.kitty.oauth2.server.springweb.port.OAuth2RegisteredClientRegistryPort;
import icu.jiapeng.kitty.oauth2.server.springweb.port.OAuth2ResourceOwnerPasswordPort;
import icu.jiapeng.kitty.oauth2.server.springweb.port.OAuth2AccessTokenPersistencePort;
import icu.jiapeng.kitty.oauth2.server.springweb.port.OAuth2AuthorizationCodePersistencePort;
import icu.jiapeng.kitty.oauth2.server.springweb.port.OAuth2RefreshTokenPersistencePort;
import icu.jiapeng.kitty.oauth2.server.springweb.properties.KittyOAuth2AuthorizationServerProperties;
import icu.jiapeng.kitty.oauth2.server.springweb.properties.OAuth2AuthorizationServerPropertiesConfigurationAdapter;
import icu.jiapeng.kitty.oauth2.server.springweb.web.OAuth2AuthorizationServerController;
import icu.jiapeng.kitty.oauth2.server.springweb.web.OAuth2ConsentController;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * <b>【规范】</b>无；本类为 Spring 装配。
 * <p>
 * <b>【本模块定制】</b>注册 {@link icu.jiapeng.kitty.oauth2.server.springweb.web.OAuth2AuthorizationServerController}、
 * {@link OAuth2AuthorizationServerService} 及默认 {@link OAuth2AuthorizationServerPropertiesPort}；
 * 各 SPI 端口 Bean 须由宿主模块提供。通常由 {@link icu.jiapeng.kitty.oauth2.server.springweb.autoconfigure.OAuth2AuthorizationServerSpringWebAutoConfiguration}
 * 自动导入，无需手写 {@code @Import}。
 * <p>
 * 协议规范官方文本见 {@link icu.jiapeng.kitty.oauth2.server.springweb.protocol.OAuth2OfficialSpecifications}。
 *
 * @see icu.jiapeng.kitty.oauth2.server.springweb.protocol.OAuth2OfficialSpecifications
 */
@Configuration
@EnableConfigurationProperties(KittyOAuth2AuthorizationServerProperties.class)
@Import({
        OAuth2AuthorizationServerController.class,
        OAuth2ConsentController.class
})
public class OAuth2AuthorizationServerSpringWebConfiguration {

    /**
     * 默认将插件配置属性绑定到 {@link OAuth2AuthorizationServerPropertiesPort}；宿主可自定义同名 Bean 覆盖。
     */
    @Bean
    @ConditionalOnMissingBean(OAuth2AuthorizationServerPropertiesPort.class)
    public OAuth2AuthorizationServerPropertiesPort oauth2AuthorizationServerPropertiesPort(
            KittyOAuth2AuthorizationServerProperties properties) {
        return new OAuth2AuthorizationServerPropertiesConfigurationAdapter(properties);
    }

    @Bean
    @ConditionalOnMissingBean(OAuth2ConsentStoragePort.class)
    public OAuth2ConsentStoragePort oauth2ConsentStoragePort() {
        return new InMemoryOAuth2ConsentStorageAdapter();
    }

    @Bean
    @ConditionalOnMissingBean(OAuth2ConsentUiPort.class)
    public OAuth2ConsentUiPort oauth2ConsentUiPort() {
        return new DefaultOAuth2ConsentUiAdapter();
    }

    @Bean
    public OAuth2AuthorizationServerService oauth2AuthorizationServerService(
            OAuth2RegisteredClientRegistryPort clientRegistry,
            OAuth2AuthorizationCodePersistencePort authorizationCodePersistence,
            OAuth2AccessTokenPersistencePort accessTokenPersistence,
            OAuth2RefreshTokenPersistencePort refreshTokenPersistence,
            OAuth2ResourceOwnerPasswordPort resourceOwnerPassword,
            OAuth2EndUserSessionPort endUserSession,
            OAuth2LoginNavigationPort loginNavigation,
            OAuth2AuthorizationServerPropertiesPort properties,
            OAuth2ConsentStoragePort consentStorage,
            OAuth2ConsentUiPort consentUi) {
        return new OAuth2AuthorizationServerCoreService(
                clientRegistry,
                authorizationCodePersistence,
                accessTokenPersistence,
                refreshTokenPersistence,
                resourceOwnerPassword,
                endUserSession,
                loginNavigation,
                properties,
                consentStorage,
                consentUi);
    }
}
