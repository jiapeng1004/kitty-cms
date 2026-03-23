package icu.jiapeng.kitty.oauth2.resource.springweb.autoconfigure;

import icu.jiapeng.kitty.oauth2.resource.springweb.extras.mybatisplus.MybatisPlusOAuth2AccessTokenValidationAdapter;
import icu.jiapeng.kitty.oauth2.resource.springweb.extras.mybatisplus.mapper.OAuth2AccessTokenResourceViewMapper;
import icu.jiapeng.kitty.oauth2.resource.springweb.port.OAuth2AccessTokenValidationPort;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.ObjectMapper;

import javax.sql.DataSource;

/**
 * MyBatis-Plus：仅 access_token 表只读校验 {@link OAuth2AccessTokenValidationPort}（分项与总开关回退与授权服务器一致）。
 */
@AutoConfiguration
@ConditionalOnClass(name = "com.baomidou.mybatisplus.core.mapper.BaseMapper")
public class OAuth2ResourceExtrasMybatisPlusAccessTokenValidationAutoConfiguration {

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnBean(DataSource.class)
    @ConditionalOnExpression(OAuth2ResourceAccessTokenPersistenceConditionalExpressions.EFFECTIVE_AUTO_OR_MYBATIS_PLUS)
    @MapperScan("icu.jiapeng.kitty.oauth2.resource.springweb.extras.mybatisplus.mapper")
    static class Beans {

        @Bean
        @ConditionalOnMissingBean(OAuth2AccessTokenValidationPort.class)
        OAuth2AccessTokenValidationPort oauth2AccessTokenValidationPortMybatisPlus(
                OAuth2AccessTokenResourceViewMapper accessTokenMapper, ObjectMapper objectMapper) {
            return new MybatisPlusOAuth2AccessTokenValidationAdapter(accessTokenMapper, objectMapper);
        }
    }
}
