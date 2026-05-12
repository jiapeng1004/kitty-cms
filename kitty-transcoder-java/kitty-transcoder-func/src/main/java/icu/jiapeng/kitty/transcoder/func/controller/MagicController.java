package icu.jiapeng.kitty.transcoder.func.controller;

import icu.jiapeng.kitty.transcoder.api.*;
import icu.jiapeng.kitty.transcoder.func.magic.MagicService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/transcode/magic")
@Tag(name = "魔法接口", description = "同步抽帧、图转、单目标转码，无策略不入队直接执行")
public class MagicController {

    @Resource
    private MagicService magicService;

    @Operation(summary = "同步抽帧")
    @PostMapping("/extract-frames")
    public TaskVO extractFrames(@RequestBody MagicExtractFramesRequest request) {
        return magicService.extractFrames(request);
    }

    @Operation(summary = "同步 ImageMagick 图转")
    @PostMapping("/image-convert")
    public TaskVO imageConvert(@RequestBody MagicImageConvertRequest request) {
        return magicService.imageConvert(request);
    }

    @Operation(summary = "同步单目标转码（SSE 实时进度）")
    @PostMapping(value = "/transcode/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter transcodeSse(@RequestBody MagicTranscodeRequest request) {
        SseEmitter emitter = new SseEmitter(0L);
        try {
            TaskVO result = magicService.transcode(request, progress -> {
                try {
                    emitter.send(SseEmitter.event().name("progress").data(progress));
                } catch (Exception e) {
                    emitter.completeWithError(e);
                }
            });
            emitter.send(SseEmitter.event().name("complete").data(result));
            emitter.complete();
        } catch (Exception e) {
            emitter.completeWithError(e);
        }
        return emitter;
    }

    @Operation(summary = "同步单目标转码（同步返回，无进度流）")
    @PostMapping("/transcode")
    public TaskVO transcode(@RequestBody MagicTranscodeRequest request) {
        return magicService.transcode(request, null);
    }
}
