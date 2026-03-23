package icu.jiapeng.kitty.oauth2.server.springweb.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * <b>【规范】</b>RFC 8414 Authorization Server Metadata；组件名与 JSON 成员名一致（蛇形）。
 *
 * @see <a href="https://www.rfc-editor.org/rfc/rfc8414">RFC 8414</a>
 * @see icu.jiapeng.kitty.oauth2.server.springweb.protocol.OAuth2OfficialSpecifications
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record OAuth2AuthorizationServerMetadataResponse(
        String issuer,
        String authorization_endpoint,
        String token_endpoint,
        String introspection_endpoint,
        String revocation_endpoint,
        List<String> token_endpoint_auth_methods_supported,
        List<String> response_types_supported,
        List<String> grant_types_supported,
        List<String> code_challenge_methods_supported,
        String consent_endpoint) {
}
