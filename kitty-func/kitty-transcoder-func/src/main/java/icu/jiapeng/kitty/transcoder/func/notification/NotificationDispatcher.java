package icu.jiapeng.kitty.transcoder.func.notification;

import icu.jiapeng.kitty.transcoder.api.NotificationConfig;
import icu.jiapeng.kitty.transcoder.api.TranscodeProgressNotifyVO;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 通知分发器：根据 method 将 config + vo 分发给对应的 NotificationClient。
 */
@Component
public class NotificationDispatcher {

    private final List<NotificationClient> clients;

    public NotificationDispatcher(List<NotificationClient> clients) {
        this.clients = clients != null ? clients : List.of();
    }

    /**
     * 异步分发通知，fire-and-forget。
     */
    public void dispatch(NotificationConfig config, TranscodeProgressNotifyVO vo) {
        if (config == null || config.getMethod() == null || config.getMethod().isBlank()) return;
        String method = config.getMethod().trim();
        for (NotificationClient client : clients) {
            if (client.supports(method)) {
                client.notifyAsync(config, vo);
                return;
            }
        }
    }
}
