package icu.jiapeng.kitty.oauth2.server.springweb.internal;

/**
 * Servlet {@link jakarta.servlet.http.HttpSession} 中暂存 OAuth2 流程数据的属性名。
 * <p>
 * 多实例部署时需使用 Spring Session、Redis 等共享会话存储，否则仅内存 Session 无法在节点间共享 pending 状态。
 */
public final class OAuth2ServletSessionKeys {

    /**
     * 待用户确认的授权请求（{@link icu.jiapeng.kitty.oauth2.server.springweb.model.OAuth2PendingAuthorization}）。
     * <p>
     * 由 {@code /oauth2/authorize} 在需展示 consent 时写入当前浏览器会话。
     */
    public static final String PENDING_AUTHORIZATION =
            "icu.jiapeng.kitty.oauth2.server.springweb.PENDING_AUTHORIZATION";

    private OAuth2ServletSessionKeys() {
    }
}
