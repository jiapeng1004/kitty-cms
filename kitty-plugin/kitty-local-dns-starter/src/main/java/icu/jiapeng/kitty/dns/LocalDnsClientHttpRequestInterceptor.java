package icu.jiapeng.kitty.dns;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.support.HttpRequestWrapper;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.Optional;

/**
 * 在发送 {@code @HttpExchange} 声明式请求前，按本地 dns 映射重写请求 URI 的 host/port。
 * <p>
 * 这样即使 {@code @HttpExchange(url="http://kitty-user")} 没有显式端口，
 * 也能把请求转发到 {@code kitty-user.jiapeng.asia:9701}。
 */
@RequiredArgsConstructor
public class LocalDnsClientHttpRequestInterceptor implements ClientHttpRequestInterceptor {

    /**
     * alias -> 目标 host + 端口
     */
    private final Map<String, Target> targets;

    @Override
    public org.springframework.http.client.ClientHttpResponse intercept(HttpRequest request, byte @NonNull [] body,
                                                                        @NonNull ClientHttpRequestExecution execution) throws IOException {
        URI uri = request.getURI();
        if (!StringUtils.hasText(uri.getHost())) {
            return execution.execute(request, body);
        }

        String host = uri.getHost();
        Target target = targets.get(host);
        if (target == null) {
            return execution.execute(request, body);
        }

        URI newUri = rewriteUri(uri, target);
        if (newUri == null) {
            return execution.execute(request, body);
        }

        HttpRequest modifiedRequest = new HttpRequestWrapper(request) {
            @Override
            public URI getURI() {
                return newUri;
            }
        };
        return execution.execute(modifiedRequest, body);
    }

    private URI rewriteUri(URI original, Target target) {
        try {
            // scheme + 目标 host + 目标端口，保持 path/query 不变。
            return new URI(original.getScheme(), null, target.host(), target.port(),
                    original.getRawPath(), original.getRawQuery(), original.getRawFragment());
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 目标（直接使用配置里的 host，不做 DNS 解析）。
     */
    public record Target(String host, int port) {

        public static Optional<Target> fromHostPort(String hostPort) {
            if (!StringUtils.hasText(hostPort)) {
                return Optional.empty();
            }
            String trimmed = hostPort.trim();
            // 支持 host:port 或纯 host（纯 host 时端口缺省 80）
            int idx = trimmed.lastIndexOf(':');
            String host;
            int port;
            if (idx > 0 && idx < trimmed.length() - 1) {
                String portPart = trimmed.substring(idx + 1);
                if (portPart.chars().allMatch(Character::isDigit)) {
                    host = trimmed.substring(0, idx);
                    port = Integer.parseInt(portPart);
                } else {
                    host = trimmed;
                    port = 80;
                }
            } else {
                host = trimmed;
                port = 80;
            }

            if (!StringUtils.hasText(host)) {
                return Optional.empty();
            }
            return Optional.of(new Target(host, port));
        }
    }
}

