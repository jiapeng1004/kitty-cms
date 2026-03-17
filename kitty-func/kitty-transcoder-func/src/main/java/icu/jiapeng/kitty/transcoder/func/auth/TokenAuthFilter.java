package icu.jiapeng.kitty.transcoder.func.auth;

import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import icu.jiapeng.kitty.transcoder.func.constants.TranscodeConstants;
import icu.jiapeng.kitty.transcoder.func.constants.TranscodeConstants.RedisKeys;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 认证过滤器：除白名单（如登录）外，所有请求必须通过认证。
 * 认证方式二选一：登录会话（Bearer / X-Api-Token）或 AK/SK 签名（X-Access-Key-Id + X-Signature + X-Timestamp + X-Signature-Nonce）。
 * 认证通过后将 accessKeyId 写入 request.setAttribute(TranscodeConstants.ATTR_ACCESS_KEY_ID)。
 */
@Component
@Order(1)
public class TokenAuthFilter extends OncePerRequestFilter {

    /**
     * 不需要认证的路径：方法 + 路径（小写），例如 "POST /api/auth/login"
     */
    private static final java.util.Set<String> WHITELIST = java.util.Set.of(
            "POST /api/auth/login",
            "POST /api/transcode/introspection/notification",
            "GET /api/transcode/preview"
    );

    @Resource
    private RedissonClient redissonClient;
    @Resource
    private AuthService authService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        // 1. 只对 /api/ 开头的接口做认证，其余（静态资源、前端页面等）全部放行
        if (uri == null || !uri.startsWith("/api/")) {
            return true;
        }
        // 2. /api/ 下再按 method+path 白名单放行
        String key = request.getMethod().toUpperCase() + " " + uri;
        return WHITELIST.contains(key);
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        String accessKeyId = resolveBySession(request);
        if (accessKeyId == null) {
            accessKeyId = resolveBySignature(request);
        }
        if (accessKeyId == null || accessKeyId.isBlank()) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"error\":\"未认证：请使用登录 Token（Authorization: Bearer 或 X-Api-Token）或 AK/SK 签名（X-Access-Key-Id / X-Signature / X-Timestamp / X-Signature-Nonce）\"}");
            return;
        }
        request.setAttribute(TranscodeConstants.ATTR_ACCESS_KEY_ID, accessKeyId);
        filterChain.doFilter(request, response);
    }

    /**
     * 登录会话：Authorization Bearer 或 X-Api-Token 或 query token
     */
    private String resolveBySession(HttpServletRequest request) {
        String token = null;
        String auth = request.getHeader("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            token = auth.substring(7).trim();
        }
        if (token == null || token.isEmpty()) {
            token = request.getHeader("X-Api-Token");
        }
        if (token == null || token.isEmpty()) {
            token = request.getParameter("token");
        }
        if (token == null || token.isEmpty() || !authService.validateLoginToken(token)) {
            return null;
        }
        RMap<String, Object> session = redissonClient.getMap(RedisKeys.SESSION_KEY_PREFIX + token);
        Object ak = session.get("accessKeyId");
        return ak != null ? ak.toString() : null;
    }

    /**
     * AK/SK 签名：请求头 X-Access-Key-Id, X-Signature, X-Timestamp, X-Signature-Nonce（或同名 query 参数）
     */
    private String resolveBySignature(HttpServletRequest request) {
        String ak = headerOrParam(request, "X-Access-Key-Id");
        String sig = headerOrParam(request, "X-Signature");
        String ts = headerOrParam(request, "X-Timestamp");
        String nonce = headerOrParam(request, "X-Signature-Nonce");
        if (ak.isBlank() || sig.isBlank() || ts.isBlank() || nonce.isBlank()) {
            return null;
        }
        long timestamp;
        try {
            timestamp = Long.parseLong(ts.trim());
        } catch (NumberFormatException e) {
            return null;
        }
        Map<String, String> params = new HashMap<>();
        params.put("Method", request.getMethod());
        params.put("Uri", request.getRequestURI());
        params.put("Timestamp", ts.trim());
        params.put("SignatureNonce", nonce);
        if (!authService.validateSignatureAndConsumeNonce(ak, sig, nonce, timestamp, params)) {
            return null;
        }
        return ak;
    }

    private static String headerOrParam(HttpServletRequest request, String name) {
        String v = request.getHeader(name);
        if (v != null && !v.isBlank()) return v;
        v = request.getParameter(name);
        return v != null ? v : "";
    }
}
