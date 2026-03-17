package icu.jiapeng.kitty.user.oauth2.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * OAuth2 授权类型枚举。
 * <p>
 * 该枚举用于维护系统内允许配置的授权类型，供：
 * 1. OAuth2 客户端管理的后端定义参考；
 * 2. 后续授权类型校验逻辑复用；
 * 3. 免登元信息接口返回给前端作为单一数据源。
 */
@Getter
@AllArgsConstructor
public enum Oauth2GrantType {
    /**
     * 授权码模式。
     * 适用于浏览器跳转登录场景，通常与 PKCE 或客户端密钥配合使用。
     */
    AUTHORIZATION_CODE("authorization_code", "授权码模式"),

    /**
     * 刷新令牌模式。
     * 用于客户端在 access token 过期后换取新的 access token。
     */
    REFRESH_TOKEN("refresh_token", "刷新令牌"),

    /**
     * 客户端凭证模式。
     * 适用于服务到服务调用，不涉及最终用户授权。
     */
    CLIENT_CREDENTIALS("client_credentials", "客户端凭证"),

    /**
     * 密码模式。
     * 兼容历史系统使用，通常不建议在新系统中作为首选方式。
     */
    PASSWORD("password", "密码模式"),

    /**
     * 简化模式。
     * 主要用于兼容旧系统或特殊前端场景，现代系统通常不建议继续新增使用。
     */
    IMPLICIT("implicit", "简化模式"),

    /**
     * 设备授权模式。
     * 适用于输入受限设备，如电视、终端设备等。
     */
    DEVICE_CODE("urn:ietf:params:oauth:grant-type:device_code", "设备授权");

    /**
     * 协议侧 grant_type 字符串。
     */
    private final String code;

    /**
     * 中文说明。
     */
    private final String desc;
}

