package icu.jiapeng.kitty.oauth2.resource.springweb.model;

/**
 * 访问令牌在资源侧解析后的载荷（与授权服务器持久化 JSON 字段约定一致，两模块类型独立）。
 */
public record OAuth2TokenSnapshot(String clientId, String subject, String scope) {
}
