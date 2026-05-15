/*
 * Copyright [2025] [贾鹏]
 *
 * kitty-cms采用APACHE LICENSE 2.0开源协议，您在使用过程中，需要注意以下几点：
 *
 * 1.请不要删除和修改根目录下的LICENSE文件。
 * 2.请不要删除和修改源码头部的版权声明。
 * 3.本项目代码可免费商业使用，商业使用请保留源码和相关描述文件的项目出处，作者声明等。
 * 4.分发源码时候，请注明软件出处 贾鹏: jiapeng_aoa@163.com。
 * 5.不可二次分发开源参与同类竞品，如有想法可联系 贾鹏: jiapeng_aoa@163.com商议合作。
 */
package icu.jiapeng.kitty.common.core.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Servlet 常用工具：基于当前请求的上下文（RequestContextHolder）获取 request、构建 base URL 等。
 * 需在 Spring Web 请求线程内使用。
 */
public final class CommonServletUtil {

    private CommonServletUtil() {
    }

    /**
     * 获取当前请求的 HttpServletRequest。
     *
     * @return 当前请求，非 Web 请求线程时返回 null
     */
    public static HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attrs != null ? attrs.getRequest() : null;
    }

    /**
     * 获取当前请求对应的服务根 URL：scheme + host + port（80/443 省略）+ contextPath。
     * 用于拼接回调等绝对路径（如 /open/oauth2/auth/callback/feishu）。
     *
     * @return 当前域根 URL，如 https://example.com/kitty-user；非请求线程或无法获取 request 时返回 null
     */
    public static String getCurrentBaseUrl() {
        HttpServletRequest request = getCurrentRequest();
        if (request == null) {
            return null;
        }
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int port = request.getServerPort();
        String contextPath = request.getContextPath();
        String portPart = (port == 80 && "http".equals(scheme)) || (port == 443 && "https".equals(scheme)) ? "" : ":" + port;
        return scheme + "://" + serverName + portPart + (contextPath != null ? contextPath : "");
    }
}
