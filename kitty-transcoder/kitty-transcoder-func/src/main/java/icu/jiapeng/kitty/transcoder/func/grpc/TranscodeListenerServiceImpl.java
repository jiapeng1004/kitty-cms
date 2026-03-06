package icu.jiapeng.kitty.transcoder.func.grpc;

import icu.jiapeng.kitty.transcoder.grpc.TranscodeListenerAck;
import icu.jiapeng.kitty.transcoder.grpc.TranscodeListenerServiceGrpc;
import icu.jiapeng.kitty.transcoder.grpc.TranscodeProgressEvent;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.grpc.server.service.GrpcService;

/**
 * 自省观察员：转码服务自身实现的 TranscodeListenerService，用于开发阶段无外部调用方时，
 * 可将通知目标指向本服务 gRPC 端口（如 localhost:9803），仅记录日志便于观察任务进度。
 */
@GrpcService
@Slf4j
public class TranscodeListenerServiceImpl extends TranscodeListenerServiceGrpc.TranscodeListenerServiceImplBase {

    @Override
    public void onProgress(TranscodeProgressEvent request, StreamObserver<TranscodeListenerAck> responseObserver) {
        try {
            log.info("[Introspection] taskId={} status={} progress={}% outputPath={} outputHttpUrl={} errorMessage={}",
                    request.getTaskId(),
                    request.getStatus(),
                    request.getProgress(),
                    request.getOutputPath().isEmpty() ? "-" : request.getOutputPath(),
                    request.getOutputHttpUrl().isEmpty() ? "-" : request.getOutputHttpUrl(),
                    request.getErrorMessage().isEmpty() ? "-" : request.getErrorMessage());
            responseObserver.onNext(TranscodeListenerAck.newBuilder().setSuccess(true).build());
        } catch (Exception e) {
            log.warn("[Introspection] onProgress failed: {}", e.getMessage());
            responseObserver.onNext(TranscodeListenerAck.newBuilder().setSuccess(false).build());
        } finally {
            responseObserver.onCompleted();
        }
    }
}
