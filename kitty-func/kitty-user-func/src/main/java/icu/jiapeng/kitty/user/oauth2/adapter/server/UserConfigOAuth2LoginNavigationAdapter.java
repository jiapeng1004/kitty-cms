package icu.jiapeng.kitty.user.oauth2.adapter.server;

import icu.jiapeng.kitty.oauth2.server.springweb.port.OAuth2AuthorizationServerPropertiesPort;
import icu.jiapeng.kitty.oauth2.server.springweb.port.OAuth2LoginNavigationPort;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 未登录跳转登录页：使用 {@link OAuth2AuthorizationServerPropertiesPort#resolveLoginPageUrl} 拼 oauth2_redirect。
 */
@Component
public class UserConfigOAuth2LoginNavigationAdapter implements OAuth2LoginNavigationPort {

    private final OAuth2AuthorizationServerPropertiesPort properties;

    public UserConfigOAuth2LoginNavigationAdapter(OAuth2AuthorizationServerPropertiesPort properties) {
        this.properties = properties;
    }

    @Override
    public void redirectToLogin(HttpServletRequest request, HttpServletResponse response, String resumeAuthorizeUrl)
            throws IOException {
        String login = properties.resolveLoginPageUrl(request);
        String target = login + (login.contains("?") ? "&" : "?")
                + "oauth2_redirect=" + URLEncoder.encode(resumeAuthorizeUrl, StandardCharsets.UTF_8);
        response.sendRedirect(target);
    }
}
