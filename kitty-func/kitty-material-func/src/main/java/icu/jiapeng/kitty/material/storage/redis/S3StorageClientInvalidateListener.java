package icu.jiapeng.kitty.material.storage.redis;

import icu.jiapeng.kitty.material.storage.entity.S3StorageDriver;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * 订阅 {@link MaterialStorageRedisPubSubChannels#S3_STORAGE_CLIENT_INVALIDATE}，静默驱逐本地缓存的 S3 客户端。
 */
@Component
@RequiredArgsConstructor
public class S3StorageClientInvalidateListener implements MessageListener {

    private final S3StorageDriver s3StorageDriver;

    @Override
    public void onMessage(@NonNull Message message, byte[] pattern) {
        message.getBody();
        if (message.getBody().length == 0) {
            return;
        }
        String storageId = new String(message.getBody(), StandardCharsets.UTF_8).trim();
        if (!storageId.isEmpty()) {
            s3StorageDriver.invalidateCachedClient(storageId);
        }
    }
}
