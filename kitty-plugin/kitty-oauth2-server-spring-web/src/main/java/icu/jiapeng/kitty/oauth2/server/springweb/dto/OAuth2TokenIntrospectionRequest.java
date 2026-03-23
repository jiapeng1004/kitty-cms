package icu.jiapeng.kitty.oauth2.server.springweb.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;

/**
 * RFC 7662 令牌自省请求（{@code application/x-www-form-urlencoded}）；字段名与规范一致（蛇形）。
 * <p>
 * {@code token_type_hint} 的<strong>取值</strong>常量见 {@link icu.jiapeng.kitty.oauth2.server.springweb.protocol.OAuth2Rfc7662TokenTypeHints}。
 *
 * @see <a href="https://www.rfc-editor.org/rfc/rfc7662">RFC 7662</a>
 */
@FieldNameConstants
@Getter
@Setter
@NoArgsConstructor
public class OAuth2TokenIntrospectionRequest {

    private String token;
    private String token_type_hint;
    private String client_id;
    private String client_secret;
}
