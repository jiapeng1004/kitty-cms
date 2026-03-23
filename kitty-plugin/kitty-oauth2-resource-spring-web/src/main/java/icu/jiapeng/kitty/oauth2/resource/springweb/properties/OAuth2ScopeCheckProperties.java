package icu.jiapeng.kitty.oauth2.resource.springweb.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 资源侧 Bearer scope 校验（与 {@code kitty.oauth2.authorization-server} 下的 {@code scope-check} 键一致）。
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "kitty.oauth2.authorization-server.scope-check")
public class OAuth2ScopeCheckProperties {

    /**
     * 是否注册 scope 校验拦截器与 {@code OAuth2ScopeCheck} Bean。
     */
    private boolean enabled = true;

    /**
     * 承载 {@code Bearer} access_token 的请求头；空则使用 {@code Authorization}（RFC 6750 常见形态）。
     */
    private String bearerHeaderName = "";
}
