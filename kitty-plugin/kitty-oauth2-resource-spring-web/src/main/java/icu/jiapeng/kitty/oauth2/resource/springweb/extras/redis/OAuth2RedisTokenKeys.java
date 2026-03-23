package icu.jiapeng.kitty.oauth2.resource.springweb.extras.redis;

/**
 * OAuth2 访问令牌在 Redis 中的键前缀（只读校验与授权服务器写入约定一致）。
 */
public final class OAuth2RedisTokenKeys {

    private OAuth2RedisTokenKeys() {
    }

    public static final String PREFIX_ACCESS_TOKEN = "oauth2:server:at:";
}
