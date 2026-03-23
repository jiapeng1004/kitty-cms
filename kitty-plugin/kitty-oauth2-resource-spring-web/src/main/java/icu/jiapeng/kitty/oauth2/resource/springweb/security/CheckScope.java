package icu.jiapeng.kitty.oauth2.resource.springweb.security;

import icu.jiapeng.kitty.oauth2.resource.springweb.model.OAuth2TokenSnapshot;
import icu.jiapeng.kitty.oauth2.resource.springweb.port.OAuth2AccessTokenValidationPort;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 在 Controller 类或方法上声明：当前请求须携带有效的 OAuth2 access_token（Bearer），且 scope 满足约定。
 * <p>
 * 令牌从请求头解析（默认 {@code Authorization: Bearer}，可配置 {@code kitty.oauth2.authorization-server.scope-check.bearer-header-name}），
 * 载荷经 {@link OAuth2AccessTokenValidationPort#resolveAccessToken(String)} 加载；
 * scope 取自 {@link OAuth2TokenSnapshot#scope()} 空格分隔串。
 * <p>
 * Service 层请注入 {@link OAuth2ScopeCheck} 并调用其 {@code require*} 方法；校验失败抛出 {@link OAuth2ScopeCheckException}，由 {@link OAuth2ScopeCheckExceptionAdvice} 转为 HTTP 响应。
 *
 * @see OAuth2ScopeCheck
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CheckScope {

    /**
     * 需要的 scope 名称；为空表示<strong>仅校验 Bearer 令牌存在且可解析</strong>（不检查具体 scope）。
     */
    String[] value() default {};

    /**
     * {@link #value()} 非空时，多个 scope 的匹配策略。
     */
    OAuth2ScopeCheckMode mode() default OAuth2ScopeCheckMode.ALL;
}
