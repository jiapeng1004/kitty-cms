package icu.jiapeng.kitty.oauth2.server.springweb.properties;

/**
 * 由 extras 提供的 {@link icu.jiapeng.kitty.oauth2.server.springweb.port.OAuth2EndUserSessionPort} 实现选型
 * （授权页判断浏览器是否已登录）。
 * <p>
 * 配置键：{@code kitty.oauth2.authorization-server.extras.end-user-session}（YAML：{@code auto}、{@code satoken}、{@code spring-security}、{@code none}），
 * 与 {@code @ConditionalOnProperty#havingValue()} 字面量一致。
 */
public enum OAuth2ExtrasEndUserSession {

    /**
     * 在 classpath 满足时由<strong>先注册的</strong>候选抢占（当前导入顺序下 <strong>Sa-Token 先于 Spring Security</strong>）。
     */
    AUTO,

    /** 显式使用 Sa-Token（{@code havingValue = "satoken"}）。 */
    SATOKEN,

    /** 显式使用 Spring Security（{@code havingValue = "spring-security"}）。 */
    SPRING_SECURITY,

    /** 不注册 extras 提供的会话适配器；由宿主自行声明 {@code OAuth2EndUserSessionPort}。 */
    CUSTOM
}
