package icu.jiapeng.kitty.oauth2.server.springweb.autoconfigure;

import icu.jiapeng.kitty.oauth2.server.springweb.properties.OAuth2ExtrasPropertyPrefix;
import icu.jiapeng.kitty.oauth2.server.springweb.extras.mybatisplus.MybatisPlusOAuth2ConsentStorageAdapter;
import icu.jiapeng.kitty.oauth2.server.springweb.extras.mybatisplus.mapper.OAuth2UserClientConsentMapper;
import icu.jiapeng.kitty.oauth2.server.springweb.port.OAuth2ConsentStoragePort;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * MyBatis-Plus：仅用户-客户端 consent（令牌表持久化见 {@code OAuth2ServerExtrasMybatisPlusTokenPersistenceAutoConfiguration}）。
 * <p>
 * Mapper 扫描在 {@link OAuth2ServerExtrasMybatisPlusTokenPersistenceAutoConfiguration}（同包下含 consent Mapper），本类不再重复 {@code @MapperScan}。
 */
@AutoConfiguration
@ConditionalOnClass(name = "com.baomidou.mybatisplus.core.mapper.BaseMapper")
public class OAuth2ExtrasMybatisPlusAutoConfiguration {

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnBean(DataSource.class)
    @ConditionalOnExpression(
            "'${kitty.oauth2.authorization-server.extras.consent-storage:auto}'.equals('auto') || "
                    + "'${kitty.oauth2.authorization-server.extras.consent-storage:auto}'.equals('mybatis-plus')")
    static class OAuth2ConsentMybatisPlusConfiguration {

        @Bean
        @ConditionalOnMissingBean(OAuth2ConsentStoragePort.class)
        OAuth2ConsentStoragePort oauth2ConsentStoragePortMybatisPlus(OAuth2UserClientConsentMapper mapper) {
            return new MybatisPlusOAuth2ConsentStorageAdapter(mapper);
        }
    }
}
