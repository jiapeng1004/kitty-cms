package icu.jiapeng.kitty.material.transcode.entity;

import icu.jiapeng.kitty.transcoder.grpc.MagicExtractFramesReq;
import icu.jiapeng.kitty.transcoder.grpc.MagicImageConvertReq;
import icu.jiapeng.kitty.transcoder.grpc.TaskResp;
import icu.jiapeng.kitty.transcoder.grpc.TranscodeServiceGrpc;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;

/**
 * kitty-transcoder 魔法接口（同步抽帧/图转），供封面、雪碧图等编排使用。
 */
@Slf4j
@Component
public class MaterialTranscodeMagicClient {

    @GrpcClient("kitty-transcoder")
    private TranscodeServiceGrpc.TranscodeServiceBlockingStub transcodeStub;

    public TaskResp magicExtractFrames(MagicExtractFramesReq req) {
        return transcodeStub.magicExtractFrames(req);
    }

    public TaskResp magicImageConvert(MagicImageConvertReq req) {
        return transcodeStub.magicImageConvert(req);
    }
}
