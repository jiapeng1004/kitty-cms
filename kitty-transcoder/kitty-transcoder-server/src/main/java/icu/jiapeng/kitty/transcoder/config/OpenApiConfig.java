package icu.jiapeng.kitty.transcoder.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI transcoderOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Kitty Transcoder API")
                        .description("媒体转码服务 REST API：任务、策略、进度、预览、认证")
                        .version("1.0")
                        .contact(new Contact().name("Kitty CMS")));
    }
}
