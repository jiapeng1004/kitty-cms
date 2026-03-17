package icu.jiapeng.kitty.dns;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 本地 DNS 映射配置：
 * <p>
 * 从 YAML 读取 `alias -> host:port`，启动时用于重写声明式 HTTP 客户端请求的 host/port。
 * <p>
 * YAML 形态（与用户需求一致）例如：
 * <pre>
 * kitty:
 *   local-dns:
 *     kitty-user: kitty-user.jiapeng.asia:9701
 * </pre>
 * <p>
 * 也可以额外支持特殊键：
 * - `enabled`: true/false（可选，默认按是否存在映射判断）
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "kitty")
public class LocalDnsProperties {

    /**
     * `kitty.local-dns` 下的所有键值。
     * <p>
     * 键包括：
     * - 业务 alias -> host:port
     * - 预留键：enabled / hostsFilePath
     */
    private Map<String, String> localDns = new HashMap<>();

    public boolean isEnabled() {
        // 如果用户显式配置 enabled，则以其为准。
        String enabledStr = localDns.get("enabled");
        if (enabledStr != null) {
            return Boolean.parseBoolean(enabledStr);
        }
        // 否则只要存在至少一个有效映射就认为启用。
        return !getMappings().isEmpty();
    }

    /**
     * 实际的 alias -> host:port 映射（已过滤预留键）。
     */
    public Map<String, String> getMappings() {
        if (localDns == null || localDns.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, String> result = new HashMap<>();
        for (Map.Entry<String, String> entry : localDns.entrySet()) {
            String key = entry.getKey();
            if (key == null) {
                continue;
            }
            if ("enabled".equals(key)) {
                continue;
            }
            String value = entry.getValue();
            if (value != null) {
                result.put(key, value);
            }
        }
        return result;
    }
}

