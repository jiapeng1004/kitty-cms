package icu.jiapeng.kitty.oauth2.resource.springweb.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 识别 {@link CheckScope} 并委托 {@link OAuth2ScopeCheck}。
 */
@RequiredArgsConstructor
public class OAuth2CheckScopeHandlerInterceptor implements HandlerInterceptor {

    private final OAuth2ScopeCheck scopeCheck;

    @Override
    public boolean preHandle(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Object handler) {
        if (!(handler instanceof HandlerMethod hm)) {
            return true;
        }
        CheckScope ann = hm.getMethodAnnotation(CheckScope.class);
        if (ann == null) {
            ann = hm.getBeanType().getAnnotation(CheckScope.class);
        }
        if (ann == null) {
            return true;
        }
        String[] scopes = ann.value();
        if (scopes.length == 0) {
            scopeCheck.requireValidBearer(request);
            return true;
        }
        if (ann.mode() == OAuth2ScopeCheckMode.ALL) {
            scopeCheck.requireAllScopes(request, scopes);
        } else {
            scopeCheck.requireAnyScope(request, scopes);
        }
        return true;
    }
}
