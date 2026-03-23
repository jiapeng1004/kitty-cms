package icu.jiapeng.kitty.oauth2.server.springweb.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * OAuth2 授权服务器 Spring Web 插件的配置（与业务 user 模块解耦）。
 * <p>
 * 绑定前缀：{@code kitty.oauth2.authorization-server}
 * <p>
 * {@code issuer} 与 RFC 8414 元数据字段 {@code issuer} 对应；其余为 <b>【本模块定制】</b>配置键名。
 *
 * @see <a href="https://www.rfc-editor.org/rfc/rfc8414">RFC 8414</a>
 * @see icu.jiapeng.kitty.oauth2.server.springweb.protocol.OAuth2OfficialSpecifications
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "kitty.oauth2.authorization-server")
public class KittyOAuth2AuthorizationServerProperties {

    /**
     * 授权服务器 issuer（RFC 8414；为空则从请求 URL 推导）。
     *
     * @see <a href="https://www.rfc-editor.org/rfc/rfc8414#section-2">RFC 8414 §2 Metadata</a>
     */
    private String issuer = "";

    /**
     * 未登录访问 /oauth2/authorize 时跳转的登录页（相对当前 context-path 或绝对 URL）。
     */
    private String loginPageUrl = "/login";

    /**
     * 授权码有效期（秒）。RFC 6749 未规定具体数值；{@code <= 0} 时在适配器中回退为 600。
     */
    private int authorizationCodeTtlSeconds = 600;

    /**
     * 可选 extras：会话 / 令牌 / consent 由 {@link Extras#getEndUserSession()}、{@link Extras#getTokenPersistence()}、分项持久化键、{@link Extras#getConsentStorage()} 等选型；
     * Sa-Token / Spring Security 仅保留细项（Cookie、请求头等），不再使用 {@code enabled=true} 式布尔开关。
     */
    @NestedConfigurationProperty
    private Extras extras = new Extras();

    @Getter
    @Setter
    public static class Extras {

        /**
         * 授权码 / access_token / refresh_token 的<strong>总开关</strong>（与 Sa-Token、Spring Security 的会话适配无关）。
         * 未设置分项 {@code access-token-persistence} 等时，三个持久化端口均回退到此值。
         * 默认 {@link OAuth2ExtrasTokenPersistence#AUTO}。
         */
        private OAuth2ExtrasTokenPersistence tokenPersistence = OAuth2ExtrasTokenPersistence.AUTO;

        /**
         * 分项：仅 access_token 持久化；未配置时继承 {@link #tokenPersistence}
         */
        private OAuth2ExtrasTokenPersistence accessTokenPersistence;

        /**
         * 分项：仅 refresh_token 持久化；未配置时继承 {@link #tokenPersistence}。
         */
        private OAuth2ExtrasTokenPersistence refreshTokenPersistence;

        /**
         * 分项：仅 authorization code 持久化；未配置时继承 {@link #tokenPersistence}。
         */
        private OAuth2ExtrasTokenPersistence authorizationCodePersistence;

        /**
         * 授权页「是否已登录」由哪套实现提供（与令牌持久化、consent 无关）。
         * 取值与 {@code @ConditionalOnProperty#havingValue()} 对齐：{@code auto}、{@code satoken}、{@code spring-security}、{@code none}。
         */
        private OAuth2ExtrasEndUserSession endUserSession = OAuth2ExtrasEndUserSession.AUTO;

        /**
         * consent 是否落 MyBatis-Plus 表；{@link OAuth2ExtrasConsentStorage#IN_MEMORY} 为进程内内存。
         */
        private OAuth2ExtrasConsentStorage consentStorage = OAuth2ExtrasConsentStorage.AUTO;

        @NestedConfigurationProperty
        private Satoken satoken = new Satoken();

        @NestedConfigurationProperty
        private SpringSecurity springSecurity = new SpringSecurity();

        @Getter
        @Setter
        public static class Satoken {

        }

        @Getter
        @Setter
        public static class SpringSecurity {

            /**
             * 若设置，则<strong>优先</strong>从该请求头读取主体 id（如网关注入的 {@code X-User-Id}）；为空则按适配器默认顺序
             * （{@code HttpServletRequest#getRemoteUser()}、{@code getUserPrincipal()}、{@code SecurityContextHolder}）。
             */
            private String principalRequestHeaderName = "";
        }
    }
}
