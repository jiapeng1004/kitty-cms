package icu.jiapeng.kitty.plugin.s3.config;

import icu.jiapeng.kitty.plugin.s3.controller.S3Controller;
import icu.jiapeng.kitty.plugin.s3.service.S3ObjectService;
import icu.jiapeng.kitty.plugin.s3.util.JaxbXmlMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * S3兼容层自动配置类
 */
@Configuration
@ConditionalOnProperty(prefix = "kitty.s3", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(S3Properties.class)
@ComponentScan(basePackages = "icu.jiapeng.kitty.plugin.s3")
public class S3AutoConfiguration {

    @Bean
    public JaxbXmlMapper jaxbXmlMapper() {
        return new JaxbXmlMapper();
    }

    @Bean
    public S3Controller s3Controller(S3ObjectService s3ObjectService, JaxbXmlMapper jaxbXmlMapper) {
        return new S3Controller(s3ObjectService, jaxbXmlMapper);
    }
}
