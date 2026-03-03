package icu.jiapeng.kitty.transcoder.func.notification;

import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * HTTP 通知客户端，使用 OkHttp3 发送任务进度回调。
 * 所有方法均不抛出异常，保证不影响转码任务执行。
 */
@Component
public class HttpNotificationClient {

    private static final Logger log = LoggerFactory.getLogger(HttpNotificationClient.class);
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .writeTimeout(5, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build();

    /**
     * 异步 POST JSON 到指定 URL，fire-and-forget。
     * 永不抛出异常，失败时仅记录日志。
     */
    public void postJsonAsync(String url, String jsonBody) {
        try {
            Request request = new Request.Builder()
                    .url(url)
                    .post(RequestBody.create(jsonBody, JSON))
                    .build();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    log.debug("HTTP notification failed: {} - {}", url, e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) {
                    response.close();
                }
            });
        } catch (Exception e) {
            log.debug("HTTP notification setup failed: {} - {}", url, e.getMessage());
        }
    }
}
