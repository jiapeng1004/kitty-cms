package icu.jiapeng.kitty.transcoder.func.notification;

import icu.jiapeng.kitty.transcoder.api.NotificationConfig;
import icu.jiapeng.kitty.transcoder.api.TranscodeProgressNotifyVO;

/**
 * 通知客户端统一接口：method + target + notifyVo。
 * HTTP、gRPC 等实现均接收相同的输入。
 */
public interface NotificationClient {

    /**
     * 是否支持该通知方式
     */
    boolean supports(String method);

    /**
     * 异步发送通知，fire-and-forget，永不抛出异常。
     *
     * @param config method + target
     * @param vo     统一通知载荷
     */
    void notifyAsync(NotificationConfig config, TranscodeProgressNotifyVO vo);
}
