package icu.jiapeng.kitty.oauth2.resource.springweb.autoconfigure;

import icu.jiapeng.kitty.oauth2.resource.springweb.extras.introspection.OAuth2IntrospectionAccessTokenValidationAdapter;
import icu.jiapeng.kitty.oauth2.resource.springweb.port.OAuth2AccessTokenValidationPort;
import icu.jiapeng.kitty.oauth2.resource.springweb.properties.OAuth2ResourceIntrospectionProperties;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * {@code extras.access-token-persistence=center}（或回退的 {@code token-persistence=center}）：RFC 7662 中心化自省 → {@link OAuth2AccessTokenValidationPort}。
 */
@AutoConfiguration
@EnableConfigurationProperties(OAuth2ResourceIntrospectionProperties.class)
public class OAuth2ResourceExtrasCenterAccessTokenValidationAutoConfiguration {

    /**
     * 自省 HTTP 使用的 {@link RestTemplate} Bean 名；宿主可声明同名 Bean 覆盖（例如 OkHttp 请求工厂）。
     */
    public static final String OAUTH2_TOKEN_INTROSPECTION_REST_TEMPLATE_BEAN_NAME = "oauth2TokenIntrospectionRestTemplate";

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnExpression(OAuth2ResourceAccessTokenPersistenceConditionalExpressions.EFFECTIVE_EQ_CENTER)
    static class CenterBeans {

        @Bean(name = OAUTH2_TOKEN_INTROSPECTION_REST_TEMPLATE_BEAN_NAME)
        @ConditionalOnMissingBean(name = OAUTH2_TOKEN_INTROSPECTION_REST_TEMPLATE_BEAN_NAME)
        RestTemplate oauth2TokenIntrospectionRestTemplate() {
            RestTemplate t = new RestTemplate();
            t.setErrorHandler(
                    new DefaultResponseErrorHandler() {
                        @Override
                        public boolean hasError(@NonNull ClientHttpResponse response) throws IOException {
                            return false;
                        }
                    });
            return t;
        }

        @Bean
        @ConditionalOnMissingBean(OAuth2AccessTokenValidationPort.class)
        OAuth2AccessTokenValidationPort oauth2AccessTokenValidationPortCenter(
                @Qualifier(OAUTH2_TOKEN_INTROSPECTION_REST_TEMPLATE_BEAN_NAME) RestTemplate oauth2TokenIntrospectionRestTemplate,
                ObjectMapper objectMapper,
                OAuth2ResourceIntrospectionProperties introspectionProperties) {
            return new OAuth2IntrospectionAccessTokenValidationAdapter(
                    oauth2TokenIntrospectionRestTemplate, objectMapper, introspectionProperties);
        }
    }
}
