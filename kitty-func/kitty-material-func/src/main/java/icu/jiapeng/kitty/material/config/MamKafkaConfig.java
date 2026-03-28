package icu.jiapeng.kitty.material.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;

/**
 * Kafka 检索同步等能力：与 {@link MamFuncConfig} {@code @Import} 引入。
 * <p>{@link org.springframework.kafka.core.KafkaTemplate}、监听器容器等由 Spring Boot Kafka 自动配置提供。</p>
 */
@Configuration
@EnableKafka
public class MamKafkaConfig {
}
