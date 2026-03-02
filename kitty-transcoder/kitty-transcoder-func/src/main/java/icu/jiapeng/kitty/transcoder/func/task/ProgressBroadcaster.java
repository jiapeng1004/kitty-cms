package icu.jiapeng.kitty.transcoder.func.task;

import com.alibaba.fastjson.JSON;
import icu.jiapeng.kitty.transcoder.api.ProgressVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
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

    public void broadcast(ProgressVO progress) {
        if (progress == null) return;
        String data = JSON.toJSONString(progress);
        for (SseEmitter e : emitters) {
            try {
                e.send(SseEmitter.event().name("progress").data(data));
            } catch (IOException ex) {
                emitters.remove(e);
            }
        }
    }
}
