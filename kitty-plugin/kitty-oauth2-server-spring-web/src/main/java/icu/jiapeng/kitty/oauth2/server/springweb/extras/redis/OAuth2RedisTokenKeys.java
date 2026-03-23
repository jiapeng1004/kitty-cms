package icu.jiapeng.kitty.oauth2.server.springweb.extras.redis;

/**
 * OAuth2 令牌在 Redis 中的键前缀（授权码 / access / refresh）。
 * <p>
 * 与资源模块只读校验使用的键前缀约定一致，两模块互不依赖。
 */
public final class OAuth2RedisTokenKeys {

    private OAuth2RedisTokenKeys() {
    }

    public static final String PREFIX_AUTH_CODE = "oauth2:server:code:";
    public static final String PREFIX_ACCESS_TOKEN = "oauth2:server:at:";
    public static final String PREFIX_REFRESH_TOKEN = "oauth2:server:rt:";
}
