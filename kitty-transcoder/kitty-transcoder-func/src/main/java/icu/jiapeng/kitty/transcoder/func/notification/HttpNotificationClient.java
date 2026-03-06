package icu.jiapeng.kitty.transcoder.func.notification;

import com.alibaba.fastjson.JSON;
import icu.jiapeng.kitty.transcoder.api.NotificationConfig;
import icu.jiapeng.kitty.transcoder.api.TranscodeProgressNotifyVO;
import okhttp3.*;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * HTTP 通知客户端，POST JSON 到 target URL。
 * 与 gRPC 共用 TranscodeProgressNotifyVO，格式一致。
 */
@Component
public class HttpNotificationClient implements NotificationClient {

    private static final Logger log = LoggerFactory.getLogger(HttpNotificationClient.class);
    private static final MediaType JSON_MEDIA = MediaType.parse("application/json; charset=utf-8");

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .writeTimeout(5, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build();

    @Override
    public boolean supports(String method) {
        return "HTTP".equalsIgnoreCase(method);
    }

    @Override
    public void notifyAsync(NotificationConfig config, TranscodeProgressNotifyVO vo) {
        if (config == null || config.getTarget() == null || config.getTarget().isBlank()) return;
        String target = config.getTarget().trim();
        Thread.startVirtualThread(() -> {
            try {
                String body = JSON.toJSONString(vo);
                Request request = new Request.Builder()
                        .url(target)
                        .post(RequestBody.create(body, JSON_MEDIA))
                        .build();
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        if (log.isDebugEnabled()) {
                            log.debug("HTTP notification failed: {} - {}", target, e.getMessage());
                        }
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) {
                        response.close();
                    }
                });
            } catch (Exception e) {
                if (log.isWarnEnabled()) {
                    log.warn("HTTP notification setup failed: {} - {}", target, e.getMessage());
                }
            }
        });
    }
}
