package icu.jiapeng.kitty.oauth2.resource.springweb.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;

/**
 * 从 HTTP 请求解析 {@code Bearer} access_token（RFC 6750）。
 */
public final class OAuth2BearerTokens {

    private OAuth2BearerTokens() {
    }

    /**
     * @param authorizationHeaderName 空则使用 {@link HttpHeaders#AUTHORIZATION}
     */
    public static String extractBearer(HttpServletRequest request, String authorizationHeaderName) {
        String name = StringUtils.hasText(authorizationHeaderName) ? authorizationHeaderName.trim() : HttpHeaders.AUTHORIZATION;
        String v = request.getHeader(name);
        if (!StringUtils.hasText(v)) {
            return null;
        }
        v = v.trim();
        if (v.length() > 7 && v.regionMatches(true, 0, "Bearer ", 0, 7)) {
            String token = v.substring(7).trim();
            return StringUtils.hasText(token) ? token : null;
        }
        return null;
    }
}
