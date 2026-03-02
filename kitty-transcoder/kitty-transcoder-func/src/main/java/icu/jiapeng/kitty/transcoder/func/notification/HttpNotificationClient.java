package icu.jiapeng.kitty.transcoder.func.notification;

import okhttp3.*;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * HTTP 通知客户端，使用 OkHttp3 发送任务进度回调。
 */
@Component
public class HttpNotificationClient {

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .writeTimeout(5, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build();

    /**
     * 异步 POST JSON 到指定 URL，fire-and-forget。
     */
    public void postJsonAsync(String url, String jsonBody) {
        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(jsonBody, JSON))
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // 静默忽略，避免干扰主流程
            }

            @Override
            public void onResponse(Call call, Response response) {
                response.close();
            }
        });
    }
}
