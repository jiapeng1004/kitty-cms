package icu.jiapeng.kitty.material.message.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * 站内信新消息广播：优先 Redisson Topic（跨 Pod）；无 Redisson 时退化为本机 SSE 直推。
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MaterialInternalMessageBroadcastPublisher {

    private final ObjectProvider<RedissonClient> redissonClientProvider;
    private final MaterialInternalMessageSseRegistry sseRegistry;

    public void publishNewMessage(String receiverUserId, String messageId) {
        if (receiverUserId == null || receiverUserId.isBlank() || messageId == null || messageId.isBlank()) {
            return;
        }
        JSONObject o = new JSONObject();
        o.put("receiverUserId", receiverUserId);
        o.put("messageId", messageId);
        String payload = JSON.toJSONString(o);
        RedissonClient client = redissonClientProvider.getIfAvailable();
        if (client != null) {
            try {
                client.getTopic(MaterialInternalMessageChannels.BROADCAST_TOPIC).publish(payload);
            } catch (Exception e) {
                log.warn("material internal message topic publish failed, fallback local sse", e);
                sseRegistry.pushNewMessage(receiverUserId, messageId);
            }
        } else {
            sseRegistry.pushNewMessage(receiverUserId, messageId);
        }
    }
}
