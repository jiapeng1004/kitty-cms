package icu.jiapeng.kitty.material.message.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 按用户维度的 SSE 连接注册表（本 Pod 内）。
 */
@Component
@Slf4j
public class MaterialInternalMessageSseRegistry {

    private final ConcurrentHashMap<String, CopyOnWriteArrayList<SseEmitter>> byUser = new ConcurrentHashMap<>();

    public void register(String userId, SseEmitter emitter) {
        if (userId == null || userId.isBlank() || emitter == null) {
            return;
        }
        byUser.computeIfAbsent(userId, _ -> new CopyOnWriteArrayList<>()).add(emitter);
    }

    public void remove(String userId, SseEmitter emitter) {
        if (userId == null || userId.isBlank() || emitter == null) {
            return;
        }
        CopyOnWriteArrayList<SseEmitter> list = byUser.get(userId);
        if (list == null) {
            return;
        }
        list.remove(emitter);
        if (list.isEmpty()) {
            byUser.remove(userId, list);
        }
    }

    /**
     * 向本 Pod 上该用户的全部连接推送新消息事件（data 为 messageId）。
     */
    public void pushNewMessage(String receiverUserId, String messageId) {
        if (receiverUserId == null || receiverUserId.isBlank() || messageId == null || messageId.isBlank()) {
            return;
        }
        CopyOnWriteArrayList<SseEmitter> list = byUser.get(receiverUserId);
        if (list == null || list.isEmpty()) {
            return;
        }
        for (SseEmitter e : list) {
            try {
                e.send(SseEmitter.event().name("new-message").data(messageId));
            } catch (Exception ex) {
                log.debug("sse push failed, remove emitter userId={}", receiverUserId, ex);
                remove(receiverUserId, e);
            }
        }
    }
}
