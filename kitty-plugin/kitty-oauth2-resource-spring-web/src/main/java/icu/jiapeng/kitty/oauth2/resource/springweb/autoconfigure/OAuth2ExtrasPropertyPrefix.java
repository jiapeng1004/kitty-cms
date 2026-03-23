package icu.jiapeng.kitty.oauth2.resource.springweb.autoconfigure;

/**
 * 与 {@code @ConditionalOnProperty} 的 {@code prefix} 一致（{@code kitty.oauth2.authorization-server.extras}）。
 * 字面量须与 {@code icu.jiapeng.kitty.oauth2.server.springweb.properties.OAuth2ExtrasPropertyPrefix#EXTRAS} 相同（两模块互不依赖，仅约定对齐）。
 */
public final class OAuth2ExtrasPropertyPrefix {

    public static final String EXTRAS = "kitty.oauth2.authorization-server.extras";

    private OAuth2ExtrasPropertyPrefix() {}
}
