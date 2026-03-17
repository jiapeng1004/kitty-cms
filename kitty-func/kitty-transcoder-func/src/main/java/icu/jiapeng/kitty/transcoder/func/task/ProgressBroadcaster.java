package icu.jiapeng.kitty.transcoder.func.task;

import icu.jiapeng.kitty.transcoder.api.ProgressVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

/**
 * 广播任务进度到所有订阅的 SSE 客户端（Flux），任意任务进度更新时推送。适配 HttpExchange 声明式客户端。
 */
@Slf4j
@Component
public class ProgressBroadcaster {

    private final Sinks.Many<ProgressVO> progressSink = Sinks.many().multicast().directBestEffort();

    /**
     * 订阅全任务进度流，任意任务进度更新时收到事件。
     */
    public Flux<ServerSentEvent<ProgressVO>> subscribeAsFlux() {
        return progressSink.asFlux()
                .map(vo -> ServerSentEvent.<ProgressVO>builder()
                        .id(String.valueOf(System.currentTimeMillis()))
                        .event("progress")
                        .data(vo)
                        .build());
    }

    /**
     * 广播进度到所有 Flux 订阅者。不抛出异常，保证不影响转码引擎执行。
     */
    public void broadcast(ProgressVO progress) {
        if (progress == null) return;
        progressSink.tryEmitNext(progress);
    }
}
