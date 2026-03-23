package icu.jiapeng.kitty.oauth2.server.springweb.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.util.StringUtils;

/**
 * <b>【规范】</b>RFC 6749 §5.1 Successful Token Response；record 组件名与 JSON 成员名一致（蛇形），不依赖 {@code @JsonProperty} 映射。
 * <p>
 * <b>【本模块定制】</b>{@code token_type} 成功时固定为 {@code Bearer}（RFC 6750）。
 *
 * @see <a href="https://www.rfc-editor.org/rfc/rfc6749#section-5.1">RFC 6749 §5.1</a>
 * @see <a href="https://www.rfc-editor.org/rfc/rfc6750">RFC 6750</a>
 * @see icu.jiapeng.kitty.oauth2.server.springweb.protocol.OAuth2OfficialSpecifications
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record OAuth2TokenSuccessResponse(
        String access_token,
        String token_type,
        long expires_in,
        String refresh_token,
        String scope) {

    public static OAuth2TokenSuccessResponse accessOnly(String accessToken, long expiresInSeconds, String scopeOrNull) {
        return new OAuth2TokenSuccessResponse(accessToken, "Bearer", expiresInSeconds, null, blankToNull(scopeOrNull));
    }

    public static OAuth2TokenSuccessResponse withRefresh(
            String accessToken, long expiresInSeconds, String refreshToken, String scopeOrNull) {
        return new OAuth2TokenSuccessResponse(accessToken, "Bearer", expiresInSeconds, refreshToken, blankToNull(scopeOrNull));
    }

    private static String blankToNull(String s) {
        return StringUtils.hasText(s) ? s : null;
    }
}
