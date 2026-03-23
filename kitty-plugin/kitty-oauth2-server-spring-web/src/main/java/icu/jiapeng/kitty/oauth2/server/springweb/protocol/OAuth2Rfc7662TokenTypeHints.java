package icu.jiapeng.kitty.oauth2.server.springweb.protocol;

/**
 * RFC 7662/7009 {@code token_type_hint} 常用取值（非表单字段名，故用普通字面量常量）。
 *
 * @see <a href="https://www.rfc-editor.org/rfc/rfc7662">RFC 7662</a>
 */
public final class OAuth2Rfc7662TokenTypeHints {

    private OAuth2Rfc7662TokenTypeHints() {
    }

    public static final String ACCESS_TOKEN = "access_token";
    public static final String REFRESH_TOKEN = "refresh_token";
}
