package icu.jiapeng.kitty.user.filter;


import icu.jiapeng.kitty.user.constans.Constant;
import icu.jiapeng.kitty.user.scope.TenScoped;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;

import java.io.IOException;

/**
 *
 *
 * @author jiapeng
 * @since 2026/2/11
 */
public class TenFilter implements Filter {
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        // 获取请求头
        if (servletRequest instanceof HttpServletRequest httpServletRequest) {
            String tenantId = httpServletRequest.getHeader(Constant.X_TENANT_ID);
            if (StringUtils.hasText(tenantId)) {
                TenScoped.run(tenantId, ()->{
                    try {
                        filterChain.doFilter(servletRequest, servletResponse);
                    } catch (IOException | ServletException e) {
                        throw new RuntimeException(e);
                    }
                });
                return;
            }
        }
        filterChain.doFilter(servletRequest, servletResponse);
    }
}
