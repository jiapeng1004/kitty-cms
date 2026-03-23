package icu.jiapeng.kitty.oauth2.resource.springweb.security;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/**
 * 将 {@link OAuth2ScopeCheckException} 转为 JSON（与常见 OAuth2 风格错误字段对齐）。
 */
@RestControllerAdvice
public class OAuth2ScopeCheckExceptionAdvice {

    @ExceptionHandler(OAuth2ScopeCheckException.class)
    public ResponseEntity<Map<String, String>> handle(OAuth2ScopeCheckException ex) {
        String msg = ex.getMessage() == null ? "" : ex.getMessage();
        return ResponseEntity.status(ex.getHttpStatus())
                .body(Map.of(
                        "error", ex.getErrorCode(),
                        "error_description", msg));
    }
}
