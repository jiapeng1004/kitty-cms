package icu.jiapeng.kitty.oauth2.resource.springweb.security;

import icu.jiapeng.kitty.oauth2.resource.springweb.model.OAuth2TokenSnapshot;
import icu.jiapeng.kitty.oauth2.resource.springweb.port.OAuth2AccessTokenValidationPort;
import icu.jiapeng.kitty.oauth2.resource.springweb.properties.OAuth2ScopeCheckProperties;
import icu.jiapeng.kitty.oauth2.resource.springweb.protocol.OAuth2ScopeStrings;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * OAuth2 access_token scope 校验的<strong>可编程入口</strong>，供 Service 等无注解场景调用；
 * {@link CheckScope} 在 Controller 上委托本类。
 */
@RequiredArgsConstructor
public class OAuth2ScopeCheck {

    private final OAuth2AccessTokenValidationPort accessTokenValidation;
    private final OAuth2ScopeCheckProperties scopeCheckProperties;

    /**
     * 必须存在可解析的 Bearer access_token。
     */
    public void requireValidBearer(HttpServletRequest request) {
        resolveOrThrow(request);
    }

    /**
     * 令牌须包含<strong>全部</strong>给定 scope。
     */
    public void requireAllScopes(HttpServletRequest request, String... scopes) {
        if (scopes == null || scopes.length == 0) {
            requireValidBearer(request);
            return;
        }
        OAuth2TokenSnapshot snap = resolveOrThrow(request);
        Set<String> granted = OAuth2ScopeStrings.parseSpaceSeparated(snap.scope());
        Set<String> need = new HashSet<>(Arrays.asList(scopes));
        if (!granted.containsAll(need)) {
            throw OAuth2ScopeCheckException.insufficientScope(
                    "need all scopes: " + need + ", granted: " + granted);
        }
    }

    /**
     * 令牌须包含<strong>至少一个</strong>给定 scope。
     */
    public void requireAnyScope(HttpServletRequest request, String... scopes) {
        if (scopes == null || scopes.length == 0) {
            requireValidBearer(request);
            return;
        }
        OAuth2TokenSnapshot snap = resolveOrThrow(request);
        Set<String> granted = OAuth2ScopeStrings.parseSpaceSeparated(snap.scope());
        for (String s : scopes) {
            if (StringUtils.hasText(s) && granted.contains(s.trim())) {
                return;
            }
        }
        throw OAuth2ScopeCheckException.insufficientScope(
                "need any of scopes: " + Arrays.toString(scopes) + ", granted: " + granted);
    }

    private OAuth2TokenSnapshot resolveOrThrow(HttpServletRequest request) {
        String raw = OAuth2BearerTokens.extractBearer(request, scopeCheckProperties.getBearerHeaderName());
        if (!StringUtils.hasText(raw)) {
            request.removeAttribute(OAuth2RequestAttributes.ACCESS_TOKEN_SNAPSHOT);
            throw OAuth2ScopeCheckException.missingBearer();
        }
        Optional<OAuth2TokenSnapshot> snap = accessTokenValidation.resolveAccessToken(raw.trim());
        if (snap.isEmpty()) {
            request.removeAttribute(OAuth2RequestAttributes.ACCESS_TOKEN_SNAPSHOT);
            throw OAuth2ScopeCheckException.invalidToken();
        }
        request.setAttribute(OAuth2RequestAttributes.ACCESS_TOKEN_SNAPSHOT, snap.get());
        return snap.get();
    }
}
