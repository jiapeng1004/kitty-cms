package icu.jiapeng.kitty.material.transcode.entity;

import icu.jiapeng.kitty.material.transcode.TranscodeDispatchGateway;
import icu.jiapeng.kitty.material.transcode.TranscodePlatforms;
import icu.jiapeng.kitty.material.transcode.TranscodeSubmitCommand;
import icu.jiapeng.kitty.transcoder.grpc.CreateTaskReq;
import icu.jiapeng.kitty.transcoder.grpc.CreateTaskResp;
import icu.jiapeng.kitty.transcoder.grpc.TranscodeServiceGrpc;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * 8.2 默认经 kitty-transcoder gRPC {@link icu.jiapeng.kitty.transcoder.grpc.TranscodeServiceGrpc} 下发任务。
 * 需在应用中配置 {@code grpc.client.kitty-transcoder}。
 */
@Slf4j
@Component
public class GrpcKittyTranscodeDispatchGateway implements TranscodeDispatchGateway {

    @GrpcClient("kitty-transcoder")
    protected TranscodeServiceGrpc.TranscodeServiceBlockingStub transcodeStub;

    @Override
    public Optional<String> submit(String platformCode, TranscodeSubmitCommand command) {
        if (platformCode == null || command == null) {
            return Optional.empty();
        }
        if (!TranscodePlatforms.KITTY_TRANSCODER_GRPC.equalsIgnoreCase(platformCode.trim())
                && !"kitty-transcoder-grpc".equalsIgnoreCase(platformCode.trim())) {
            log.warn("unsupported transcode platform: {}", platformCode);
            return Optional.empty();
        }
        try {
            CreateTaskReq.Builder b = CreateTaskReq.newBuilder()
                    .setInputType(command.getInputType())
                    .setInputPath(command.getInputPath())
                    .setStrategyId(command.getExternalStrategyId())
                    .setPriority(command.getPriority());
            if (command.getExtraParamsJson() != null && !command.getExtraParamsJson().isBlank()) {
                b.setExtraParamsJson(command.getExtraParamsJson());
            }
            CreateTaskResp resp = transcodeStub.createTask(b.build());
            String taskId = resp.getTaskId();
            if (taskId.isBlank()) {
                return Optional.empty();
            }
            return Optional.of(taskId);
        } catch (Exception e) {
            log.warn("transcoder gRPC createTask failed", e);
            return Optional.empty();
        }
    }
}
