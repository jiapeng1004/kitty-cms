package icu.jiapeng.kitty.oauth2.server.springweb.autoconfigure;

import icu.jiapeng.kitty.oauth2.server.springweb.properties.OAuth2ExtrasPropertyPrefix;
import icu.jiapeng.kitty.oauth2.server.springweb.extras.satoken.SaTokenOAuth2EndUserSessionAdapter;
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
 * Sa-Token 可选集成：提供 {@link OAuth2EndUserSessionPort} 默认实现。
 * <p>
 * 显式选型：{@code extras.end-user-session=satoken}（{@link ConditionalOnProperty#havingValue()} 为平台字面量）；
 * 自动抢占：{@code extras.end-user-session=auto}（{@code havingValue = "auto"}）与导入顺序决定。
 */
@AutoConfiguration
@EnableConfigurationProperties(KittyOAuth2AuthorizationServerProperties.class)
public class OAuth2ExtrasSaTokenAutoConfiguration {

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(name = "cn.dev33.satoken.stp.StpUtil")
    @ConditionalOnProperty(
            prefix = OAuth2ExtrasPropertyPrefix.EXTRAS,
            name = "end-user-session",
            havingValue = "satoken")
    static class SaTokenOAuth2EndUserSessionExplicitConfiguration {

        @Bean
        @ConditionalOnMissingBean(OAuth2EndUserSessionPort.class)
        OAuth2EndUserSessionPort oauth2EndUserSessionPortSaTokenExplicit() {
            return new SaTokenOAuth2EndUserSessionAdapter();
        }
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(name = "cn.dev33.satoken.stp.StpUtil")
    @ConditionalOnProperty(
            prefix = OAuth2ExtrasPropertyPrefix.EXTRAS,
            name = "end-user-session",
            havingValue = "auto")
    static class SaTokenOAuth2EndUserSessionAutoConfiguration {

        @Bean
        @ConditionalOnMissingBean(OAuth2EndUserSessionPort.class)
        OAuth2EndUserSessionPort oauth2EndUserSessionPortSaTokenAuto() {
            return new SaTokenOAuth2EndUserSessionAdapter();
        }
    }
}
