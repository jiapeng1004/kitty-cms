package icu.jiapeng.kitty.user.oauth2.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * OAuth2 客户端在令牌端点的认证方式（token_endpoint_auth_method），与 OIDC 注册约定一致。
 * <p>
 * 用于维护系统内允许配置的认证方式，供客户端管理与元信息接口返回。
 */
@Getter
@AllArgsConstructor
public enum Oauth2ClientAuthenticationMethod {
    /**
     * HTTP Basic：Authorization 头中 Base64(client_id:client_secret)。
     */
    CLIENT_SECRET_BASIC("client_secret_basic", "HTTP Basic（client_secret_basic）"),

    /**
     * POST 表单体中传递 client_id、client_secret。
     */
    CLIENT_SECRET_POST("client_secret_post", "POST 表单（client_secret_post）"),

    /**
     * 使用对称密钥签名的 JWT 客户端断言。
     */
    CLIENT_SECRET_JWT("client_secret_jwt", "JWT 断言—密钥（client_secret_jwt）"),

    /**
     * 使用私钥签名的 JWT 客户端断言。
     */
    PRIVATE_KEY_JWT("private_key_jwt", "JWT 断言—私钥（private_key_jwt）"),

    /**
     * 公开客户端，无 client_secret（如纯前端）。
     */
    NONE("none", "无密钥（none，公开客户端）");

    /**
     * 协议侧取值，与 OAuth2/OIDC 注册字段一致。
     */
    private final String code;

    /**
     * 中文说明。
     */
    private final String desc;
}
