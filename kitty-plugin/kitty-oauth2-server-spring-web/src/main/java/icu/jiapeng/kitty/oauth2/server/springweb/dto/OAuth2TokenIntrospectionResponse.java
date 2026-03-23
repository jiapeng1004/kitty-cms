package icu.jiapeng.kitty.oauth2.server.springweb.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * RFC 7662 OAuth 2.0 Token Introspection 响应；组件名与 JSON 成员名一致（蛇形）。
 *
 * @see <a href="https://www.rfc-editor.org/rfc/rfc7662">RFC 7662</a>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public record OAuth2TokenIntrospectionResponse(
        Boolean active,
        String scope,
        String client_id,
        String username,
        String token_type,
        String sub,
        Long exp,
        Long iat) {

    public static OAuth2TokenIntrospectionResponse inactive() {
        return new OAuth2TokenIntrospectionResponse(false, null, null, null, null, null, null, null);
    }
}
