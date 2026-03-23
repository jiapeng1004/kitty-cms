package icu.jiapeng.kitty.oauth2.server.springweb.internal;

import icu.jiapeng.kitty.oauth2.server.springweb.model.OAuth2ConsentViewModel;
import icu.jiapeng.kitty.oauth2.server.springweb.port.OAuth2ConsentUiPort;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * 从 classpath 读取 {@code oauth2/consent-page.html}，占位符替换后输出 HTML。
 */
public class DefaultOAuth2ConsentUiAdapter implements OAuth2ConsentUiPort {

    private volatile String template;

    @Override
    public void renderConsentPage(HttpServletRequest request, HttpServletResponse response, OAuth2ConsentViewModel model)
            throws IOException {
        if (template == null) {
            synchronized (this) {
                if (template == null) {
                    ClassPathResource res = new ClassPathResource("oauth2/consent-page.html");
                    try (InputStream in = res.getInputStream()) {
                        template = StreamUtils.copyToString(in, StandardCharsets.UTF_8);
                    }
                }
            }
        }
        String scopesJson = scopesJson(model.scopes());
        String html = template
                .replace("{{CONTEXT_PATH}}", model.contextPath() == null ? "" : model.contextPath())
                .replace("{{CLIENT_ID}}", escapeHtml(model.clientId()))
                .replace("{{SCOPE_DISPLAY}}", escapeHtml(model.scopeDisplay()))
                .replace("{{SCOPES_JSON}}", scopesJson);
        response.setContentType("text/html;charset=UTF-8");
        response.getWriter().write(html);
    }

    private static String scopesJson(java.util.List<String> scopes) {
        if (scopes == null || scopes.isEmpty()) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < scopes.size(); i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append('"').append(jsonEscape(scopes.get(i))).append('"');
        }
        sb.append(']');
        return sb.toString();
    }

    private static String jsonEscape(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static String escapeHtml(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
