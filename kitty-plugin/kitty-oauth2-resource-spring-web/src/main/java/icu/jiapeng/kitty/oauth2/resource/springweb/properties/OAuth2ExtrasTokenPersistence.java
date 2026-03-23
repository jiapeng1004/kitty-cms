package icu.jiapeng.kitty.oauth2.resource.springweb.properties;

import java.util.Locale;

/**
 * 与授权服务器 {@code kitty.oauth2.authorization-server.extras.token-persistence} 绑定的<strong>同名枚举</strong>，YAML 字面量须与
 * {@code icu.jiapeng.kitty.oauth2.server.springweb.properties.OAuth2ExtrasTokenPersistence} 一致；
 * 两模块<strong>互不依赖</strong>，仅约定对齐。
 * <p>
 * 资源侧仅关心 access_token：优先 {@code access-token-persistence}，未设置时继承 {@code token-persistence}。
 * <p>
 * 资源侧据此选择 {@link icu.jiapeng.kitty.oauth2.resource.springweb.port.OAuth2AccessTokenValidationPort} 的实现（Redis 只读、MyBatis 只读、中心化自省等）。
 */
public enum OAuth2ExtrasTokenPersistence {

    /**
     * 在 classpath 满足时由<strong>先注册的</strong>候选抢占（与授权服务器一致：Redis 先于 MyBatis-Plus）。
     */
    AUTO,

    /** 显式使用 Redis 键约定做只读校验（{@code havingValue = "redis"}）。 */
    REDIS,

    /** 显式使用 MyBatis-Plus access_token 表做只读校验（{@code havingValue = "mybatis-plus"}）。 */
    MYBATIS_PLUS,

    /**
     * 中心化校验：RFC 7662 自省（需配置 {@code kitty.oauth2.resource.introspection.*}）。资源<strong>独立部署</strong>且未显式配置 access/legacy 键时，默认即为此取值（见 {@link icu.jiapeng.kitty.oauth2.resource.springweb.env.OAuth2ResourceTokenPersistenceDefaultEnvironmentPostProcessor}）。
     */
    CENTER,

    /**
     * 不注册 extras 提供的 {@link icu.jiapeng.kitty.oauth2.resource.springweb.port.OAuth2AccessTokenValidationPort}；
     * 由宿主自行提供（例如与授权服务器同进程时委托 {@code OAuth2AccessTokenPersistencePort#getAccessToken}）。
     */
    NONE;

    public static OAuth2ExtrasTokenPersistence fromConfigurationValue(String raw) {
        if (raw == null || raw.isBlank()) {
            return AUTO;
        }
        String s = raw.trim().toLowerCase(Locale.ROOT);
        return switch (s) {
            case "auto" -> AUTO;
            case "redis" -> REDIS;
            case "mybatis-plus" -> MYBATIS_PLUS;
            case "center" -> CENTER;
            case "none" -> NONE;
            default -> throw new IllegalArgumentException("Unknown OAuth2 extras token persistence: " + raw);
        };
    }

    public String toYamlString() {
        return switch (this) {
            case AUTO -> "auto";
            case REDIS -> "redis";
            case MYBATIS_PLUS -> "mybatis-plus";
            case CENTER -> "center";
            case NONE -> "none";
        };
    }
}
