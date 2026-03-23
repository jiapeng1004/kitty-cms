package icu.jiapeng.kitty.oauth2.resource.springweb.security;

/**
 * {@link CheckScope#mode()}：要求注解中列出的 scope 与令牌中已授权 scope 的匹配方式。
 */
public enum OAuth2ScopeCheckMode {

    /**
     * 令牌须<strong>包含全部</strong>列出的 scope。
     */
    ALL,

    /**
     * 令牌须<strong>至少包含其一</strong>列出的 scope。
     */
    ANY
}
