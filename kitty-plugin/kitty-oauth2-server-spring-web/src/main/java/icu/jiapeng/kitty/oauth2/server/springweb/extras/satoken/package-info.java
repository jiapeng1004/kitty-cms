/**
 * Sa-Token extras：为 {@link icu.jiapeng.kitty.oauth2.server.springweb.port.OAuth2EndUserSessionPort}
 * 提供基于 {@code cn.dev33.satoken.stp.StpUtil} 的默认实现。
 *
 * <h3>职责</h3>
 *
 * <p>授权码流程中，授权端点需要知道<strong>当前浏览器会话</strong>是否已登录、以及资源所有者主体 id。
 * 本适配器将「是否登录 / 主体 id」委托给 Sa-Token 会话，与 OAuth2 访问令牌（Bearer）体系<strong>解耦</strong>：
 * 浏览器侧仍走 Sa-Token Cookie/Header 等既有登录态。
 *
 * <h3>何时注册为 Bean</h3>
 *
 * <p>由 {@link icu.jiapeng.kitty.oauth2.server.springweb.autoconfigure.OAuth2ExtrasSaTokenAutoConfiguration}
 * 在以下条件<strong>同时</strong>满足时注册 {@link icu.jiapeng.kitty.oauth2.server.springweb.extras.satoken.SaTokenOAuth2EndUserSessionAdapter}：
 * <ul>
 *   <li>{@code extras.end-user-session} 为 {@code satoken} 或 {@code auto}（见 {@code OAuth2ExtrasSaTokenAutoConfiguration}）；</li>
 *   <li>classpath 上存在类 {@code cn.dev33.satoken.stp.StpUtil}（即宿主已引入 {@code sa-token-core} 或含该类的 starter）；</li>
 *   <li>容器中尚不存在 {@code OAuth2EndUserSessionPort} 类型的 Bean。</li>
 * </ul>
 *
 * <h3>与 Spring Security extras 的优先级</h3>
 *
 * <p>若宿主<strong>同时</strong>引入 Sa-Token 与 Spring Security，且两者均未自定义
 * {@code OAuth2EndUserSessionPort}：自动配置会<strong>优先</strong>注册本包适配器。原因是
 * {@code AutoConfiguration.imports} 中 Sa-Token 的自动配置类<strong>排在</strong> Security
 * <strong>之前</strong>，先执行的配置在满足条件时先占位 {@code @ConditionalOnMissingBean}。
 *
 * <p>若仅需 Security、不需要 Sa-Token：不要引入 {@code sa-token-core}，则仅 Security 适配器会生效（若 classpath 有 {@code SecurityContextHolder}）。
 *
 * @see icu.jiapeng.kitty.oauth2.server.springweb.extras.springsecurity
 */
package icu.jiapeng.kitty.oauth2.server.springweb.extras.satoken;
