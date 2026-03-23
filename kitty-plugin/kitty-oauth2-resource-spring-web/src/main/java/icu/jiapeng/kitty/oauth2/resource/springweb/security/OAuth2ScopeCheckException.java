package icu.jiapeng.kitty.oauth2.resource.springweb.security;

import lombok.Getter;

/**
 * Bearer 缺失、令牌无效或 scope 不足时抛出，由 {@link OAuth2ScopeCheckExceptionAdvice} 转为 HTTP 响应。
 */
@Getter
public class OAuth2ScopeCheckException extends RuntimeException {

    private final int httpStatus;
    private final String errorCode;

    public OAuth2ScopeCheckException(int httpStatus, String errorCode, String message) {
        super(message);
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
    }

    public static OAuth2ScopeCheckException missingBearer() {
        return new OAuth2ScopeCheckException(401, "invalid_request", "missing Authorization Bearer access_token");
    }

    public static OAuth2ScopeCheckException invalidToken() {
        return new OAuth2ScopeCheckException(401, "invalid_token", "access_token unknown or expired");
    }

    public static OAuth2ScopeCheckException insufficientScope(String detail) {
        return new OAuth2ScopeCheckException(403, "insufficient_scope", detail);
    }
}
