package icu.jiapeng.kitty.oauth2.server.springweb.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;

/**
 * <b>【规范】</b>RFC 6749 §4.1.1 授权端点查询参数；字段名与规范键名一致（蛇形）。
 *
 * @see <a href="https://www.rfc-editor.org/rfc/rfc6749">RFC 6749</a>
 * @see <a href="https://www.rfc-editor.org/rfc/rfc7636">RFC 7636</a>
 * @see icu.jiapeng.kitty.oauth2.server.springweb.protocol.OAuth2OfficialSpecifications
 */
@FieldNameConstants
@Getter
@Setter
public class OAuth2AuthorizeRequest {

    private String response_type;
    private String client_id;
    private String redirect_uri;
    private String scope;
    private String state;
    private String code_challenge;
    private String code_challenge_method;
}
