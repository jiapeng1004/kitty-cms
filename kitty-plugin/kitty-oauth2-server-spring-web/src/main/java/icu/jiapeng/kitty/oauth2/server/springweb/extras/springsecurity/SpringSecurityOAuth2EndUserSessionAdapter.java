package icu.jiapeng.kitty.oauth2.server.springweb.extras.springsecurity;

import icu.jiapeng.kitty.oauth2.server.springweb.port.OAuth2EndUserSessionPort;
import icu.jiapeng.kitty.oauth2.server.springweb.properties.KittyOAuth2AuthorizationServerProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;

import jakarta.servlet.http.HttpServletRequest;

import java.security.Principal;
import java.util.Optional;

/**
 * 【可选默认】授权/consent 端点终端用户：依次尝试 {@link HttpServletRequest#getRemoteUser()}、
 * {@link HttpServletRequest#getUserPrincipal()}，再回退 {@link SecurityContextHolder}（须保证过滤器链已建立认证）。
 * <p>
 * 须 {@code extras.end-user-session} 为 {@code spring-security} 或 {@code auto}。在 Sa-Token 与 Security
 * 的 extras 均启用且均未自定义 Port 时，自动配置优先注册 Sa-Token 适配器；本类作为仅启用 Security extras 时的实现。
 */
@RequiredArgsConstructor
public class SpringSecurityOAuth2EndUserSessionAdapter implements OAuth2EndUserSessionPort {

    private final KittyOAuth2AuthorizationServerProperties properties;

    @Override
    public Optional<String> currentUserId(HttpServletRequest request) {
        String configuredHeader = properties.getExtras().getSpringSecurity().getPrincipalRequestHeaderName();
        if (StringUtils.hasText(configuredHeader)) {
            String v = request.getHeader(configuredHeader.trim());
            if (StringUtils.hasText(v)) {
                return Optional.of(v.trim());
            }
        }
        String remote = request.getRemoteUser();
        if (StringUtils.hasText(remote)) {
            return Optional.of(remote);
        }
        Principal p = request.getUserPrincipal();
        if (p != null && StringUtils.hasText(p.getName())) {
            return Optional.of(p.getName());
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return Optional.empty();
        }
        return Optional.ofNullable(auth.getName());
    }
}
