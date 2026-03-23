package icu.jiapeng.kitty.oauth2.server.springweb.port;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Optional;

/**
 * 从当前 HTTP 请求识别终端用户（Cookie、请求头中的宿主 token、或线程内会话等），供 {@code /oauth2/authorize}、{@code /oauth2/consent} 等使用。
 * <p>
 * 与 OAuth2 协议中的 access_token（资源访问）解耦；实现可委托 Sa-Token、Spring Security 或完全自定义。
 */
public interface OAuth2EndUserSessionPort {

    /**
     * @param request 当前请求（须为授权端点 / consent 等同一线程上的 {@link HttpServletRequest}）
     * @return 已识别的主体 id；未登录或无法从请求建立身份时为空
     */
    Optional<String> currentUserId(HttpServletRequest request);
}
