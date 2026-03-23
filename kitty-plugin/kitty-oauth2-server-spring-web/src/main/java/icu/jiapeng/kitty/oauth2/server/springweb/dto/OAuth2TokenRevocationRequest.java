package icu.jiapeng.kitty.oauth2.server.springweb.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;

/**
 * RFC 7009 令牌撤销请求；参数名与 RFC 7662 自省请求对齐（蛇形字段名）。
 *
 * @see <a href="https://www.rfc-editor.org/rfc/rfc7009">RFC 7009</a>
 */
@FieldNameConstants
@Getter
@Setter
@NoArgsConstructor
public class OAuth2TokenRevocationRequest {

    private String token;
    private String token_type_hint;
    private String client_id;
    private String client_secret;
}
