package icu.jiapeng.kitty.oauth2.resource.springweb.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * RFC 7662 OAuth 2.0 Token Introspection 成功/失败响应 JSON（蛇形字段名）。
 * <p>
 * {@code active == false} 时仅序列化 {@code active}；其余元数据仅在 {@code active == true} 时出现。
 *
 * @see <a href="https://www.rfc-editor.org/rfc/rfc7662">RFC 7662</a>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public record OAuth2TokenIntrospectionResponse(
        @JsonProperty("active") Boolean active,
        @JsonProperty("scope") String scope,
        @JsonProperty("client_id") String clientId,
        @JsonProperty("username") String username,
        @JsonProperty("token_type") String tokenType,
        @JsonProperty("sub") String sub,
        @JsonProperty("exp") Long exp,
        @JsonProperty("iat") Long iat) {

    public static OAuth2TokenIntrospectionResponse inactive() {
        return new OAuth2TokenIntrospectionResponse(false, null, null, null, null, null, null, null);
    }
}
