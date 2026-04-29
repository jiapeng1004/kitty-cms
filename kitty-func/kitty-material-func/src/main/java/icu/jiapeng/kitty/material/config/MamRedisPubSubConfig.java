package icu.jiapeng.kitty.material.config;

import icu.jiapeng.kitty.material.storage.redis.MaterialStorageRedisPubSubChannels;
import icu.jiapeng.kitty.material.storage.redis.S3StorageClientInvalidateListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

/**
 * Redis 发布订阅：存储配置变更时多 Pod 协同失效内存缓存等。
 */
@Configuration
public class MamRedisPubSubConfig {

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory connectionFactory,
            S3StorageClientInvalidateListener s3StorageClientInvalidateListener) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(
                s3StorageClientInvalidateListener,
                new ChannelTopic(MaterialStorageRedisPubSubChannels.S3_STORAGE_CLIENT_INVALIDATE));
        return container;
    }
}
