/**
 * <h2>extras：可选默认 SPI 实现</h2>
 *
 * <p>本包及子包存放与 <strong>Sa-Token</strong>、<strong>Spring Security</strong>、<strong>Redis</strong>、
 * <strong>MyBatis-Plus</strong> 集成的<strong>可选</strong>适配器类。它们<strong>不</strong>构成核心 OAuth2 契约的一部分。
 * <strong>首要条件</strong>：在配置中设置
 * {@code …extras.end-user-session}、{@code …extras.token-persistence}、{@code …extras.consent-storage}（取值与 {@code @ConditionalOnProperty#havingValue()} 字面量一致，无 {@code enabled=true} 式布尔开关）；
 * <strong>授权页会话</strong>由 Sa-Token 还是 Spring Security 提供由
 * {@code …extras.end-user-session}（{@code auto} / {@code satoken} / {@code spring-security} / {@code none}）标明；
 * <strong>授权码 / access / refresh</strong> 由
 * {@code …extras.token-persistence}（{@code auto} / {@code redis} / {@code mybatis-plus} / {@code center} / {@code none}）标明；
 * <strong>consent</strong> 是否用 MyBatis-Plus 表由 {@code …extras.consent-storage}（{@code auto} / {@code in-memory}）标明。
 * 仅 classpath 存在 Sa-Token 等<strong>不会</strong>自动注册适配器。在满足配置开关的前提下，还须宿主在
 * {@code pom.xml} 中<strong>显式引入</strong>对应依赖且存在触发类，再由
 * {@code OAuth2Extras*} 自动配置注册 Bean。
 *
 * <h3>Maven 与传递性</h3>
 *
 * <p>{@code kitty-oauth2-server-spring-web} 对本模块的 Sa-Token / Security / Redis / MyBatis-Plus 相关依赖声明为
 * <strong>{@code optional=true}</strong>（不传递给依赖本 starter 的应用）。因此：
 * <ul>
 *   <li>仅引入本 starter 时，运行时<strong>不会</strong>自动带上上述三方库；</li>
 *   <li>若需要某套 extras 默认实现，应用须在<strong>自己的</strong> {@code pom.xml} 中再声明同一
 *       坐标（如 {@code cn.dev33:sa-token-core}、{@code org.springframework.security:spring-security-core}、
 *       {@code spring-boot-starter-data-redis}、{@code com.baomidou:mybatis-plus-spring-boot4-starter}）。</li>
 * </ul>
 *
 * <h3>自动配置顺序与 {@code @ConditionalOnMissingBean}</h3>
 *
 * <p>各 extras 的 {@code @AutoConfiguration} 在
 * {@code META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports} 中登记，
 * 且<strong>列在</strong> {@code OAuth2AuthorizationServerSpringWebAutoConfiguration} <strong>之前</strong>。
 * 主 OAuth2 配置使用 {@code @AutoConfiguration(after = { …Extras… })}，确保 extras 先完成可选 Bean 注册。
 *
 * <p>各 extras 在注册端口 Bean 时使用 {@code @ConditionalOnMissingBean(对应Port.class)}：
 * 宿主只要自行提供同类型 {@code @Bean}，即可<strong>完全覆盖</strong>默认实现，无需排除自动配置。
 *
 * <h3>子包索引</h3>
 *
 * <dl>
 *   <dt>{@link icu.jiapeng.kitty.oauth2.server.springweb.extras.satoken}</dt>
 *   <dd>{@code OAuth2EndUserSessionPort} → Sa-Token {@code StpUtil}</dd>
 *   <dt>{@link icu.jiapeng.kitty.oauth2.server.springweb.extras.springsecurity}</dt>
 *   <dd>{@code OAuth2EndUserSessionPort} → {@code SecurityContextHolder}（无 Sa-Token 或与 Sa-Token 并存时的后备顺序见该包说明）</dd>
 *   <dt>{@link icu.jiapeng.kitty.oauth2.server.springweb.autoconfigure.OAuth2ServerExtrasRedisTokenPersistenceAutoConfiguration}</dt>
 *   <dd>{@link icu.jiapeng.kitty.oauth2.server.springweb.port.OAuth2AuthorizationCodePersistencePort} /
 *       {@link icu.jiapeng.kitty.oauth2.server.springweb.port.OAuth2AccessTokenPersistencePort} /
 *       {@link icu.jiapeng.kitty.oauth2.server.springweb.port.OAuth2RefreshTokenPersistencePort} → {@code StringRedisTemplate} + Jackson</dd>
 *   <dt>{@link icu.jiapeng.kitty.oauth2.server.springweb.autoconfigure.OAuth2ServerExtrasMybatisPlusTokenPersistenceAutoConfiguration}</dt>
 *   <dd>同上三个端口 → 授权码 / access / refresh 表（见本模块 {@code schema/kt-oauth2-token-tables.mysql.sql}）</dd>
 *   <dt>{@link icu.jiapeng.kitty.oauth2.server.springweb.extras.mybatisplus}</dt>
 *   <dd>本模块内仅 {@code OAuth2ConsentStoragePort} → 表 {@code kt_oauth2_user_client_consent}</dd>
 * </dl>
 *
 * <p>更完整的说明（Maven 片段、条件表、Redis 键前缀、与 OAuth2 Bearer / 会话 token 的关系）见模块根目录
 * {@code README.md} 中「extras」章节。
 */
package icu.jiapeng.kitty.oauth2.server.springweb.extras;
