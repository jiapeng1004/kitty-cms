package icu.jiapeng.kitty.oauth2.resource.springweb.security;

/**
 * {@link jakarta.servlet.http.HttpServletRequest#setAttribute(String, Object)} 键名：OAuth2 scope 校验解析结果。
 */
public final class OAuth2RequestAttributes {

    /**
     * 类型 {@link icu.jiapeng.kitty.oauth2.resource.springweb.model.OAuth2TokenSnapshot}，由 {@link OAuth2ScopeCheck} 在成功解析 Bearer access_token 后写入。
     */
    public static final String ACCESS_TOKEN_SNAPSHOT =
            "icu.jiapeng.kitty.oauth2.resource.springweb.security.ACCESS_TOKEN_SNAPSHOT";

    private OAuth2RequestAttributes() {
    }
}
