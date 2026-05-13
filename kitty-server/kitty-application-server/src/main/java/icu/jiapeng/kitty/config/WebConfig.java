package icu.jiapeng.kitty.config;

import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 配置mvc
 *
 * @author Jiapeng
 */
//@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        // 为 products 包下的所有 Controller 自动添加 /api/products 前缀
        configurer.addPathPrefix("/api/kitty-user",
                handler -> handler.getPackage().getName().startsWith("icu.jiapeng.kitty.user"));

        // 为 users 包下的所有 Controller 自动添加 /api/users 前缀
        configurer.addPathPrefix("/api/kitty-mam",
                handler -> handler.getPackage().getName().startsWith("icu.jiapeng.kitty.material"));
    }
}