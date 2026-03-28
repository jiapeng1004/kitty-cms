package icu.jiapeng.kitty.material.config;

import java.util.Optional;

/**
 * 配置中心端口：按命名空间读取字符串配置（具体 key 组合由实现决定）。
 */
public interface ConfigCenterGateway {

    /**
     * @param namespace 逻辑命名空间（如 {@code material}）
     * @param key         命名空间内键
     * @return 未配置或下游不可用时为空
     */
    Optional<String> getString(String namespace, String key);
}
