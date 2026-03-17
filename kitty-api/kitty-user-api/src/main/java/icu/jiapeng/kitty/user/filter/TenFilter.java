package icu.jiapeng.kitty.user.filter;


import icu.jiapeng.kitty.user.constans.Constant;
import icu.jiapeng.kitty.user.scope.TenScoped;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 *
 *
 * @author jiapeng
 * @since 2026/2/11
 */
@Component
public class TenFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest servletRequest, @NonNull HttpServletResponse servletResponse, FilterChain filterChain) throws ServletException, IOException {
        // 获取请求头
        String tenantId = servletRequest.getHeader(Constant.X_TENANT_ID);
        if (StringUtils.hasText(tenantId)) {
            TenScoped.run(tenantId, () -> {
                try {
                    filterChain.doFilter(servletRequest, servletResponse);
                } catch (IOException | ServletException e) {
                    throw new RuntimeException(e);
                }
            });
            return;
        }
        filterChain.doFilter(servletRequest, servletResponse);
    }
}
