package icu.jiapeng.kitty.transcoder.func.config;

import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.support.spring.FastJsonRedisSerializer;
import org.redisson.codec.JsonJackson3Codec;
import org.redisson.spring.starter.RedissonAutoConfigurationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean(name = {"redisTemplate"})
    public RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<Object, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        redisTemplate.setKeySerializer(stringRedisSerializer);
        redisTemplate.setHashKeySerializer(stringRedisSerializer);
        FastJsonRedisSerializer<Object> fastJsonSerializer = new FastJsonRedisSerializer<>(Object.class);
        fastJsonSerializer.getFastJsonConfig()
                .getParserConfig()
                .setAutoTypeSupport(true)
        ;
        fastJsonSerializer.getFastJsonConfig().setSerializerFeatures(
                SerializerFeature.WriteClassName,
                SerializerFeature.WriteEnumUsingToString,
                SerializerFeature.WriteMapNullValue,
                SerializerFeature.WriteDateUseDateFormat,
                SerializerFeature.WriteNullListAsEmpty,
                SerializerFeature.WriteNullStringAsEmpty,
                SerializerFeature.WriteNullBooleanAsFalse,
                SerializerFeature.WriteNullNumberAsZero,
                SerializerFeature.DisableCircularReferenceDetect
        );
        redisTemplate.setValueSerializer(fastJsonSerializer);
        redisTemplate.setHashValueSerializer(fastJsonSerializer);
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

    @Bean
    public RedissonAutoConfigurationCustomizer myRedissonAutoConfigurationCustomizer() {
        return config -> {
            config.setCodec(new JsonJackson3Codec());
        };
    }
}
