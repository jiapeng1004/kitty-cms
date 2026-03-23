package icu.jiapeng.kitty.oauth2.resource.springweb.autoconfigure;

/**
 * 与 {@link icu.jiapeng.kitty.oauth2.resource.springweb.properties.OAuth2ResourceAccessTokenPersistenceEffective} 对齐的占位符，
 * 供 {@link org.springframework.boot.autoconfigure.condition.ConditionalOnExpression} 使用。
 */
public final class OAuth2ResourceAccessTokenPersistenceConditionalExpressions {

    private static final String E = "kitty.oauth2.authorization-server.extras";
    private static final String ACCESS = "${" + E + ".access-token-persistence:${" + E + ".token-persistence:auto}}";

    public static final String EFFECTIVE_EQ_REDIS = "'" + ACCESS + "' == 'redis'";
    public static final String EFFECTIVE_EQ_AUTO = "'" + ACCESS + "' == 'auto'";
    public static final String EFFECTIVE_EQ_CENTER = "'" + ACCESS + "' == 'center'";
    public static final String EFFECTIVE_AUTO_OR_MYBATIS_PLUS =
            "'" + ACCESS + "' == 'auto' || '" + ACCESS + "' == 'mybatis-plus'";

    private OAuth2ResourceAccessTokenPersistenceConditionalExpressions() {}
}
