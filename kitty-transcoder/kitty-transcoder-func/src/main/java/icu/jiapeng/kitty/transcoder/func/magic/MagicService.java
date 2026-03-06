package icu.jiapeng.kitty.transcoder.func.magic;

import icu.jiapeng.kitty.transcoder.api.*;

import java.util.function.Consumer;

/**
 * 魔法接口服务：同步抽帧、同步图转、同步单目标转码。
 * 无策略、不入队、直接执行，任务记录在列表中统一展示。
 */
public interface MagicService {

    /**
     * 同步抽帧：输入支持 HTTP 或本地文件
     * @param request 请求参数
     * @return 任务详情（含输出路径）
     */
    TaskVO extractFrames(MagicExtractFramesRequest request);

    /**
     * 同步 ImageMagick 图转
     * @param request 请求参数
     * @return 任务详情
     */
    TaskVO imageConvert(MagicImageConvertRequest request);

    /**
     * 同步单目标转码，REST 通过 SSE 获取进度
     * @param request 请求参数
     * @param progressConsumer 进度回调（SSE 推送用）
     * @return 任务详情
     */
    TaskVO transcode(MagicTranscodeRequest request, Consumer<ProgressVO> progressConsumer);
}
