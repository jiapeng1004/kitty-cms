package icu.jiapeng.kitty.oauth2.resource.springweb.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * RFC 7662 自省客户端参数（与 {@code extras.access-token-persistence=center} 或回退的 {@code extras.token-persistence=center} 组合使用）。
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "kitty.oauth2.resource.introspection")
public class OAuth2ResourceIntrospectionProperties {

    /** 自省端点绝对 URL（通常为元数据 {@code introspection_endpoint}）。 */
    private String endpoint = "";

    /** 自省客户端 ID。 */
    private String clientId = "";

    /** 自省客户端密钥。 */
    private String clientSecret = "";
}
