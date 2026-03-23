/**
 * Spring Security extras：为 {@link icu.jiapeng.kitty.oauth2.server.springweb.port.OAuth2EndUserSessionPort}
 * 提供基于 {@link org.springframework.security.core.context.SecurityContextHolder} 的默认实现。
 *
 * <h3>职责</h3>
 *
 * <p>从当前线程的 {@link org.springframework.security.core.context.SecurityContext} 读取
 * {@link org.springframework.security.core.Authentication}，若已认证且非匿名，则
 * {@link org.springframework.security.core.Authentication#getName()} 作为终端用户 id（字符串）。
 * 适用于已用 Spring Security 建立会话 / JWT 在 SecurityContext 中的场景。
 *
 * <h3>何时注册为 Bean</h3>
 *
 * <p>由 {@link icu.jiapeng.kitty.oauth2.server.springweb.autoconfigure.OAuth2ExtrasSpringSecurityAutoConfiguration}
 * 在以下条件<strong>同时</strong>满足时注册
 * {@link icu.jiapeng.kitty.oauth2.server.springweb.extras.springsecurity.SpringSecurityOAuth2EndUserSessionAdapter}：
 * <ul>
 *   <li>配置 {@code kitty.oauth2.authorization-server.extras.spring-security.enabled=true}；</li>
 *   <li>classpath 上存在 {@code org.springframework.security.core.context.SecurityContextHolder}（宿主已引入
 *       {@code spring-security-core} 或完整 Security 栈）；</li>
 *   <li>容器中尚不存在 {@code OAuth2EndUserSessionPort} 类型的 Bean。</li>
 * </ul>
 *
 * <h3>与 Sa-Token 同时存在时</h3>
 *
 * <p>若 Sa-Token extras 已先注册 {@code OAuth2EndUserSessionPort}，则本适配器<strong>不会</strong>再注册
 *（{@code @ConditionalOnMissingBean}）。若希望强制使用 Security 实现，应自行声明
 * {@code @Bean OAuth2EndUserSessionPort} 返回自定义或本类实例，并避免依赖 Sa-Token 适配器占位。
 *
 * @see icu.jiapeng.kitty.oauth2.server.springweb.extras.satoken
 */
package icu.jiapeng.kitty.oauth2.server.springweb.extras.springsecurity;
