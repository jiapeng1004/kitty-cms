package icu.jiapeng.kitty.material;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 *
 *
 * @author jiapeng
 * @since 2026/1/11
 */
@SpringBootApplication
@EnableFeignClients(basePackages = "icu.jiapeng.kitty.user.api.internal.api")
public class KittyMaterialApplication {
    static void main(String[] args) {
        SpringApplication.run(KittyMaterialApplication.class, args);
    }
}
