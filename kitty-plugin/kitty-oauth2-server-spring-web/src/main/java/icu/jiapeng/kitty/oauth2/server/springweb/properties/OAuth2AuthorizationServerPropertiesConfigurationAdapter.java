package icu.jiapeng.kitty.oauth2.server.springweb.properties;

import icu.jiapeng.kitty.oauth2.server.springweb.port.OAuth2AuthorizationServerPropertiesPort;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;

/**
 * 将 {@link KittyOAuth2AuthorizationServerProperties} 绑定到 SPI {@link OAuth2AuthorizationServerPropertiesPort}。
 *
 * @see <a href="https://www.rfc-editor.org/rfc/rfc8414">RFC 8414</a>
 */
@RequiredArgsConstructor
public class OAuth2AuthorizationServerPropertiesConfigurationAdapter implements OAuth2AuthorizationServerPropertiesPort {

    private final KittyOAuth2AuthorizationServerProperties properties;

    @Override
    public String resolveIssuer(HttpServletRequest req) {
        String iss = properties.getIssuer();
        if (StringUtils.hasText(iss)) {
            return iss.trim();
        }
        String ctx = req.getContextPath() != null ? req.getContextPath() : "";
        return req.getScheme() + "://" + req.getServerName() + ":" + req.getServerPort() + ctx;
    }

    @Override
    public String resolveLoginPageUrl(HttpServletRequest request) {
        String lp = properties.getLoginPageUrl();
        if (!StringUtils.hasText(lp)) {
            lp = "/login";
        }
        if (lp.startsWith("http://") || lp.startsWith("https://")) {
            return lp;
        }
        String ctx = request.getContextPath() != null ? request.getContextPath() : "";
        if (lp.startsWith("/")) {
            return request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + ctx + lp;
        }
        return request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + ctx + "/" + lp;
    }

    @Override
    public int authorizationCodeTtlSeconds() {
        int s = properties.getAuthorizationCodeTtlSeconds();
        return s > 0 ? s : 600;
    }
}
