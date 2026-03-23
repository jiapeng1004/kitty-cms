package icu.jiapeng.kitty.oauth2.server.springweb.port;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * <b>【规范】</b>OAuth 核心规范不定义「登录页 URL」或浏览器会话；授权端点要求用户已登录时由实现完成身份提示。
 * <p>
 * <b>【本模块定制】</b>当 {@link OAuth2EndUserSessionPort#currentUserId(HttpServletRequest)} 为空时，
 * 核心服务调用本端口将用户送去宿主登录流程；查询参数名（如宿主使用的 {@code oauth2_redirect}）由
 * <b>宿主适配器</b>决定，非 IETF OAuth 固定字段。
 * <p>
 * 与「授权前必须完成资源所有者身份认证」相关的框架性要求见：
 *
 * @see <a href="https://www.rfc-editor.org/rfc/rfc6749#section-4.1.1">RFC 6749 §4.1.1</a>（授权端点对终端用户与客户端的约束由实现完成）
 */
public interface OAuth2LoginNavigationPort {

    /**
     * 跳转至宿主登录页。
     *
     * @param request              当前请求
     * @param response             响应用于重定向
     * @param resumeAuthorizeUrl   登录成功后应继续的完整 authorize URL（含 query），<b>【本模块定制】</b>由核心服务拼接
     * @throws IOException        重定向 I/O 异常
     */
    void redirectToLogin(HttpServletRequest request, HttpServletResponse response, String resumeAuthorizeUrl) throws IOException;
}
