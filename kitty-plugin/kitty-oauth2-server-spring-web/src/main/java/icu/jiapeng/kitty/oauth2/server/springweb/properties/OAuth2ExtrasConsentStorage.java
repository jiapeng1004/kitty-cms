package icu.jiapeng.kitty.oauth2.server.springweb.properties;

/**
 * extras 是否注册 MyBatis-Plus 的 {@link icu.jiapeng.kitty.oauth2.server.springweb.port.OAuth2ConsentStoragePort}。
 * <p>
 * 配置键：{@code kitty.oauth2.authorization-server.extras.consent-storage}（YAML：{@code auto}、{@code in-memory}）。
 * 与 {@link OAuth2ExtrasTokenPersistence} 无关；进程内默认实现由核心配置在缺省 Bean 时提供。
 */
public enum OAuth2ExtrasConsentStorage {

    /**
     * {@code extras.consent-storage=auto}（与 {@code @ConditionalOnProperty#havingValue()} 为 {@code auto}）时注册 MyBatis-Plus 表实现；否则不注册 extras 的 consent Bean，
     * 由核心回退到内存实现。
     */
    AUTO,

    /**
     * 显式使用 MyBatis-Plus（{@code havingValue = "mybatis-plus"}）。
     */
    MYBATIS_PLUS,

    /**
     * 不注册 MyBatis-Plus consent 适配器，
     * 使用核心提供的 {@code InMemoryOAuth2ConsentStorageAdapter}。
     * 若仍需要 MyBatis-Plus 的令牌持久化，请与 {@code extras.token-persistence=mybatis-plus} 组合使用。
     */
    IN_MEMORY,

    /**
     * 不注册 MyBatis-Plus consent 适配器，
     * 使用自定义的 consent 适配器。
     */
    CUSTOM,
}
