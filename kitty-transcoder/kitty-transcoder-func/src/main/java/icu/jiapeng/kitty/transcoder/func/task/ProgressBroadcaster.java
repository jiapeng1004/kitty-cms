package icu.jiapeng.kitty.transcoder.func.task;

import com.alibaba.fastjson.JSON;
import icu.jiapeng.kitty.transcoder.api.ProgressVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 广播任务进度到所有订阅的 SSE 客户端。
 */
@Slf4j
@Component
public class ProgressBroadcaster {

    private static final long SSE_TIMEOUT = 0; // 不超时

    private final CopyOnWriteArrayList<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError(e -> emitters.remove(emitter));
        emitters.add(emitter);
        return emitter;
    }

    /**
     * 广播进度到所有订阅的 SSE 客户端。
     * 失败时仅移除该 emitter 并记录日志，不抛出异常，保证不影响转码引擎执行。
     */
    public void broadcast(ProgressVO progress) {
        if (progress == null) return;
        String data = JSON.toJSONString(progress);
        for (SseEmitter e : emitters) {
            try {
                e.send(SseEmitter.event().name("progress").data(data));
            } catch (Exception ex) {
                emitters.remove(e);
                log.debug("SSE send failed, emitter removed: {}", ex.getMessage());
            }
        }
    }
}
