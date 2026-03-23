package icu.jiapeng.kitty.oauth2.server.springweb.model;

import java.util.List;

/**
 * 内置 consent 页所需展示数据（pending 状态由 {@link jakarta.servlet.http.HttpSession} 持有，不在表单中传 token）。
 */
public record OAuth2ConsentViewModel(
        String clientId,
        String scopeDisplay,
        List<String> scopes,
        String contextPath
) {
}
