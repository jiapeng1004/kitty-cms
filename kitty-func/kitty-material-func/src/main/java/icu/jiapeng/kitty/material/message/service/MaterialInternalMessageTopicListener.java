package icu.jiapeng.kitty.material.message.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;

/**
 * 订阅站内信广播 Topic，将事件转为本 Pod 的 SSE 推送。
 * <p>由 {@link icu.jiapeng.kitty.material.config.MaterialRedissonIntegrationConfiguration} 注册（需存在 {@link RedissonClient}）。</p>
 */
@RequiredArgsConstructor
@Slf4j
public class MaterialInternalMessageTopicListener {

    private final RedissonClient redissonClient;
    private final MaterialInternalMessageSseRegistry sseRegistry;

    @PostConstruct
    public void subscribe() {
        try {
            RTopic topic = redissonClient.getTopic(MaterialInternalMessageChannels.BROADCAST_TOPIC);
            topic.addListener(String.class, (_, msg) -> {
                if (msg == null || msg.isBlank()) {
                    return;
                }
                try {
                    JSONObject o = JSON.parseObject(msg);
                    String receiverUserId = o.getString("receiverUserId");
                    String messageId = o.getString("messageId");
                    if (receiverUserId != null && messageId != null) {
                        sseRegistry.pushNewMessage(receiverUserId, messageId);
                    }
                } catch (Exception e) {
                    log.warn("material internal message topic payload invalid: {}", msg, e);
                }
            });
        } catch (Exception e) {
            log.warn("subscribe material internal message topic failed", e);
        }
    }
}
