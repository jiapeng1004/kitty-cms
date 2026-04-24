package icu.jiapeng.kitty.material.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 栏目排序等异步后处理（事务提交后归一化检查）。
 */
@Configuration
@EnableAsync
public class MaterialCatalogAsyncConfiguration {
}
