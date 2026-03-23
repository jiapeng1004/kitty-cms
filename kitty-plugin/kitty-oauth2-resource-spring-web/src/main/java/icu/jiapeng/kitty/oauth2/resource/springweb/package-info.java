/**
 * OAuth2 资源侧 Spring Web 支持：{@link icu.jiapeng.kitty.oauth2.resource.springweb.port.OAuth2AccessTokenValidationPort}、
 * {@link icu.jiapeng.kitty.oauth2.resource.springweb.security.CheckScope} / {@link icu.jiapeng.kitty.oauth2.resource.springweb.security.OAuth2ScopeCheck}、
 * 与授权服务器令牌持久化字面量约定对齐的枚举 {@link icu.jiapeng.kitty.oauth2.resource.springweb.properties.OAuth2ExtrasTokenPersistence}（与 server 模块同名枚举字面量一致，互不依赖）；资源侧校验 access_token 时优先 {@code access-token-persistence}，未设置时继承 {@code token-persistence}。
 * <p>
 * 仅作资源服务器或需解析开放平台 Bearer 上下文时使用；授权服务器持久化实现位于 {@code kitty-oauth2-server-spring-web}。
 */
package icu.jiapeng.kitty.oauth2.resource.springweb;
