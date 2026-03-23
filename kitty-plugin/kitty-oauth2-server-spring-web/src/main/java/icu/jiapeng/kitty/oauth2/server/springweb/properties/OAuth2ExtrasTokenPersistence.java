package icu.jiapeng.kitty.oauth2.server.springweb.properties;

/**
 * 由 extras 提供的授权码 / access_token / refresh_token 持久化实现选型（分别对应
 * {@link icu.jiapeng.kitty.oauth2.server.springweb.port.OAuth2AuthorizationCodePersistencePort}、
 * {@link icu.jiapeng.kitty.oauth2.server.springweb.port.OAuth2AccessTokenPersistencePort}、
 * {@link icu.jiapeng.kitty.oauth2.server.springweb.port.OAuth2RefreshTokenPersistencePort}）。
 * <p>
 * 总开关：{@code kitty.oauth2.authorization-server.extras.token-persistence}。亦可分项：
 * {@code access-token-persistence}、{@code refresh-token-persistence}、{@code authorization-code-persistence}（未设置时分项继承总开关）。
 * <p>
 * 资源模块定义<strong>同名枚举</strong>，取值须与本类保持一致（两模块互不依赖，仅约定对齐）。
 */
public enum OAuth2ExtrasTokenPersistence {

    /**
     * 在 classpath 满足时由<strong>先注册的</strong>候选抢占（当前导入顺序下 Redis 先于 MyBatis-Plus，故通常 Redis 优先）。
     */
    AUTO,

    /** 显式使用 Redis（{@code havingValue = "redis"}）。 */
    REDIS,

    /** 显式使用 MyBatis-Plus（{@code havingValue = "mybatis-plus"}）。 */
    MYBATIS_PLUS,

    /**
     * 不在本模块注册 extras 令牌持久化（典型于资源侧走中心化 RFC 7662 自省；授权服务器若选此项须自行提供上述三类持久化端口实现）。
     */
    CENTER,

    /**
     * 不注册 extras 提供的令牌持久化 Bean；由宿主自行声明上述三类持久化端口 Bean，
     * 或接受应用因缺少该 Port 而无法启动（取决于核心装配是否强制要求）。
     */
    NONE;
}
