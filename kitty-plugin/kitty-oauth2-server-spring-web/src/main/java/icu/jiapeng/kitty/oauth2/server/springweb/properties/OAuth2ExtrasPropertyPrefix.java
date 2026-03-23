package icu.jiapeng.kitty.oauth2.server.springweb.properties;

/**
 * 与 {@code @ConditionalOnProperty} 的 {@code prefix} 一致（{@code kitty.oauth2.authorization-server.extras}）。
 */
public final class OAuth2ExtrasPropertyPrefix {

    public static final String EXTRAS = "kitty.oauth2.authorization-server.extras";

    private static final String AC = "${" + EXTRAS + ".authorization-code-persistence:${" + EXTRAS + ".token-persistence:auto}}";
    private static final String AT = "${" + EXTRAS + ".access-token-persistence:${" + EXTRAS + ".token-persistence:auto}}";
    private static final String RT = "${" + EXTRAS + ".refresh-token-persistence:${" + EXTRAS + ".token-persistence:auto}}";

    public static final String AUTHORIZATION_CODE_EFFECTIVE_EQ_REDIS = "'" + AC + "' == 'redis'";
    public static final String AUTHORIZATION_CODE_EFFECTIVE_EQ_AUTO = "'" + AC + "' == 'auto'";
    public static final String AUTHORIZATION_CODE_EFFECTIVE_AUTO_OR_MYBATIS_PLUS =
            "'" + AC + "' == 'auto' || '" + AC + "' == 'mybatis-plus'";

    public static final String ACCESS_TOKEN_EFFECTIVE_EQ_REDIS = "'" + AT + "' == 'redis'";
    public static final String ACCESS_TOKEN_EFFECTIVE_EQ_AUTO = "'" + AT + "' == 'auto'";
    public static final String ACCESS_TOKEN_EFFECTIVE_AUTO_OR_MYBATIS_PLUS =
            "'" + AT + "' == 'auto' || '" + AT + "' == 'mybatis-plus'";

    public static final String REFRESH_TOKEN_EFFECTIVE_EQ_REDIS = "'" + RT + "' == 'redis'";
    public static final String REFRESH_TOKEN_EFFECTIVE_EQ_AUTO = "'" + RT + "' == 'auto'";
    public static final String REFRESH_TOKEN_EFFECTIVE_AUTO_OR_MYBATIS_PLUS =
            "'" + RT + "' == 'auto' || '" + RT + "' == 'mybatis-plus'";

    private OAuth2ExtrasPropertyPrefix() {
    }
}
