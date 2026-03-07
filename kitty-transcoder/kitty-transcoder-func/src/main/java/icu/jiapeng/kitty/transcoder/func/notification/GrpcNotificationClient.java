package icu.jiapeng.kitty.transcoder.func.notification;

import icu.jiapeng.kitty.transcoder.api.NotificationConfig;
import icu.jiapeng.kitty.transcoder.api.TranscodeProgressNotifyVO;
import icu.jiapeng.kitty.transcoder.grpc.TranscodeListenerServiceGrpc;
import icu.jiapeng.kitty.transcoder.grpc.TranscodeProgressEvent;
import io.grpc.Channel;
import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.grpc.client.GrpcChannelFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * gRPC 通知客户端，向 target（host:port）回调 TranscodeListenerService.OnProgress。
 * 与 HTTP 共用 TranscodeProgressNotifyVO，格式一致。
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class GrpcNotificationClient implements NotificationClient {

    private static final int DEADLINE_SECONDS = 10;

    private final GrpcChannelFactory channelFactory;

    @Override
    public boolean supports(String method) {
        return "GRPC".equalsIgnoreCase(method);
    }

    @Override
    public void notifyAsync(NotificationConfig config, TranscodeProgressNotifyVO vo) {
        if (config == null || config.getTarget() == null || config.getTarget().isBlank()) return;
        String target = config.getTarget().trim();
        // for lambda
        Thread.startVirtualThread(() -> {
            try {
                doNotify(target, vo);
            } catch (Throwable e) {
                if (log.isDebugEnabled()) {
                    log.debug("gRPC notification failed: {} - {}", target, e.getMessage());
                }
            }
        });
    }

    private void doNotify(String target, TranscodeProgressNotifyVO vo) {
        try {
            Channel channel = channelFactory.createChannel(target);
            TranscodeProgressEvent event = TranscodeProgressEvent.newBuilder()
                    .setTaskId(vo.getTaskId() != null ? vo.getTaskId() : "")
                    .setStatus(vo.getStatus() != null ? vo.getStatus() : "")
                    .setProgress(vo.getProgress() != null ? vo.getProgress() : 0)
                    .setOutputPath(vo.getOutputPath() != null ? vo.getOutputPath() : "")
                    .setOutputHttpUrl(vo.getOutputHttpUrl() != null ? vo.getOutputHttpUrl() : "")
                    .setErrorMessage(vo.getErrorMessage() != null ? vo.getErrorMessage() : "")
                    .build();
            TranscodeListenerServiceGrpc.TranscodeListenerServiceBlockingStub stub =
                    TranscodeListenerServiceGrpc.newBlockingStub(channel)
                            .withDeadlineAfter(DEADLINE_SECONDS, TimeUnit.SECONDS);
            stub.onProgress(event);
        } catch (StatusRuntimeException e) {
            if (log.isDebugEnabled()) {
                log.debug("gRPC notification failed: {} - {}", target, e.getStatus());
            }
        }
    }
}
