package icu.jiapeng.kitty.oauth2.server.springweb.model;

/**
 * 访问令牌 / 刷新令牌在存储中的载荷。
 * <p>
 * 与资源模块 {@code icu.jiapeng.kitty.oauth2.resource.springweb.model.OAuth2TokenSnapshot} 字段约定一致（JSON / Redis 互通），但二者互不依赖。
 */
public record OAuth2TokenSnapshot(String clientId, String subject, String scope) {
}
