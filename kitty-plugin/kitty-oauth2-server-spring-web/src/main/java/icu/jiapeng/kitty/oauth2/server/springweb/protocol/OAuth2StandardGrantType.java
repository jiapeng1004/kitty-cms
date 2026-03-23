package icu.jiapeng.kitty.oauth2.server.springweb.protocol;

/**
 * <b>【规范】</b>RFC 6749 附录及正文中定义的 {@code grant_type} 常用字面量（插件不依赖业务侧枚举）。
 *
 * @see <a href="https://www.rfc-editor.org/rfc/rfc6749">RFC 6749</a>
 * @see icu.jiapeng.kitty.oauth2.server.springweb.protocol.OAuth2OfficialSpecifications
 */
public final class OAuth2StandardGrantType {

    private OAuth2StandardGrantType() {
    }

    public static final String AUTHORIZATION_CODE = "authorization_code";
    public static final String REFRESH_TOKEN = "refresh_token";
    public static final String CLIENT_CREDENTIALS = "client_credentials";
    public static final String PASSWORD = "password";
}
