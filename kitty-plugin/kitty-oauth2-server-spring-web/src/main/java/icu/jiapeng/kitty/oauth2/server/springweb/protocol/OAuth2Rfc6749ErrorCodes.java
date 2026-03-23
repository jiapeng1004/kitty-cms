package icu.jiapeng.kitty.oauth2.server.springweb.protocol;

/**
 * RFC 6749 §5.2 / §4.1.2.1 等定义的 {@code error} 参数<strong>取值</strong>（非 DTO 绑定字段，故用普通字面量常量）。
 *
 * @see <a href="https://www.rfc-editor.org/rfc/rfc6749#section-5.2">RFC 6749 §5.2</a>
 */
public final class OAuth2Rfc6749ErrorCodes {

    private OAuth2Rfc6749ErrorCodes() {
    }

    public static final String INVALID_REQUEST = "invalid_request";
    public static final String INVALID_CLIENT = "invalid_client";
    public static final String INVALID_GRANT = "invalid_grant";
    public static final String UNAUTHORIZED_CLIENT = "unauthorized_client";
    public static final String UNSUPPORTED_GRANT_TYPE = "unsupported_grant_type";
    public static final String INVALID_SCOPE = "invalid_scope";
    public static final String UNSUPPORTED_RESPONSE_TYPE = "unsupported_response_type";
    public static final String ACCESS_DENIED = "access_denied";
    public static final String SERVER_ERROR = "server_error";
}
