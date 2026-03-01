package icu.jiapeng.kitty.transcoder.func.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 可选认证：从 Authorization Bearer 或 X-Api-Token 解析 Token，校验通过则设置 request 属性 accessKeyId。
 * 不拦截请求，仅用于审计或后续需要当前调用方身份时使用。
 */
@Component
@Order(1)
public class TokenAuthFilter extends OncePerRequestFilter {

    private static final String SESSION_KEY_PREFIX = "transcode:session:token:";
    public static final String ATTR_ACCESS_KEY_ID = "transcode.accessKeyId";

    @Autowired
    private RedissonClient redissonClient;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String token = null;
        String auth = request.getHeader("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            token = auth.substring(7).trim();
        }
        if (token == null || token.isEmpty()) {
            token = request.getHeader("X-Api-Token");
        }
        if (token != null && !token.isEmpty()) {
            RMap<String, Object> session = redissonClient.getMap(SESSION_KEY_PREFIX + token);
            if (session.isExists()) {
                Object ak = session.get("accessKeyId");
                if (ak != null) {
                    request.setAttribute(ATTR_ACCESS_KEY_ID, ak.toString());
                }
            }
        }
        filterChain.doFilter(request, response);
    }
}
