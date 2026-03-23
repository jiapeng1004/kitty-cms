package icu.jiapeng.kitty.oauth2.server.springweb.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;

/**
 * <b>【规范】</b>字段名与 RFC 6749 令牌端点表单键一致（蛇形）；由 Spring 按参数名绑定，不依赖 Jackson/MVC 名称映射注解。
 * <p>
 * <b>【本模块定制】</b>多 grant 合入单一类型；合法性在核心服务中按 grant 分别校验。
 *
 * @see <a href="https://www.rfc-editor.org/rfc/rfc6749">RFC 6749</a>
 * @see icu.jiapeng.kitty.oauth2.server.springweb.protocol.OAuth2OfficialSpecifications
 */
@FieldNameConstants
@Getter
@Setter
@NoArgsConstructor
public class OAuth2TokenRequest {

    private String grant_type;
    private String scope;
    private String client_id;
    private String client_secret;
    private String username;
    private String password;
    private String refresh_token;
    private String code;
    private String redirect_uri;
    private String code_verifier;
}
