package icu.jiapeng.kitty.transcoder.func.config;

import org.redisson.codec.JsonJackson3Codec;
import org.redisson.spring.starter.RedissonAutoConfigurationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedisConfig {


    @Bean
    public RedissonAutoConfigurationCustomizer myRedissonAutoConfigurationCustomizer() {
        return config -> {
            config.setCodec(new JsonJackson3Codec());
        };
    }
}
