package icu.jiapeng.kitty.oauth2.server.springweb.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.experimental.FieldNameConstants;

/**
 * <b>【规范】</b>RFC 6749 §5.2 Token Error Response；组件名与 JSON 成员名一致（蛇形）。
 * <p>
 * {@link FieldNameConstants} 仅用于本类型<strong>实际序列化字段名</strong>（{@link #Fields}）；{@code error} 的取值字面量见
 * {@link icu.jiapeng.kitty.oauth2.server.springweb.protocol.OAuth2Rfc6749ErrorCodes}。
 *
 * @see <a href="https://www.rfc-editor.org/rfc/rfc6749#section-5.2">RFC 6749 §5.2</a>
 * @see icu.jiapeng.kitty.oauth2.server.springweb.protocol.OAuth2OfficialSpecifications
 */
@FieldNameConstants
@JsonInclude(JsonInclude.Include.NON_NULL)
public record OAuth2ErrorResponse(String error, String error_description, String error_uri) {

    public OAuth2ErrorResponse(String error, String error_description) {
        this(error, error_description, null);
    }
}
