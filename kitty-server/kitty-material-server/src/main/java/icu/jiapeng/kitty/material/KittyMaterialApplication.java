package icu.jiapeng.kitty.material;


import icu.jiapeng.kitty.material.config.MaterialTranscodeProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 *
 *
 * @author jiapeng
 * @since 2026/1/11
 */
@SpringBootApplication
@EnableConfigurationProperties(MaterialTranscodeProperties.class)
public class KittyMaterialApplication {
    public static void main(String[] args) {
        SpringApplication.run(KittyMaterialApplication.class, args);
    }
}
