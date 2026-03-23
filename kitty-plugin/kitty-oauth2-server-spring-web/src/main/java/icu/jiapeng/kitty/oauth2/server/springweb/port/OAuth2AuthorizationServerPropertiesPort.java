package icu.jiapeng.kitty.oauth2.server.springweb.port;

import jakarta.servlet.http.HttpServletRequest;

/**
 * issuer / 登录页等运行环境配置（外围：配置文件、租户配置等）。
 *
 * @see <a href="https://www.rfc-editor.org/rfc/rfc8414">RFC 8414</a>（{@code issuer} 等元数据字段）
 * @see icu.jiapeng.kitty.oauth2.server.springweb.protocol.OAuth2OfficialSpecifications
 */
public interface OAuth2AuthorizationServerPropertiesPort {

    /**
     * @see <a href="https://www.rfc-editor.org/rfc/rfc8414#section-2">RFC 8414 §2</a>
     */
    String resolveIssuer(HttpServletRequest request);

    /**
     * 登录页 URL，可为相对路径（相对当前请求的 context-path）或绝对 URL。
     */
    String resolveLoginPageUrl(HttpServletRequest request);

    /**
     * 授权码有效期（秒）；配置非法时由实现回退为合理默认（如 600）。
     */
    int authorizationCodeTtlSeconds();
}
