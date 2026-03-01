package icu.jiapeng.kitty.transcoder.func.engine;

import icu.jiapeng.kitty.transcoder.api.ProbeResult;
import icu.jiapeng.kitty.transcoder.api.StrategyStepVO;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class ProbeStepExecutor implements StepExecutor {

    @Override
    public String getType() {
        return StepExecutorType.PROBE.getCode();
    }

    @Override
    public String execute(String inputPath, StrategyStepVO step, String stepSuffix, StepContext context) throws Exception {
        File input = new File(inputPath);
        if (!input.exists()) throw new java.io.IOException("输入不存在：" + inputPath);
        if (input.isDirectory()) {
            if (context != null) context.setProbeResult(context.getCurrentStepIndex(), new ProbeResult());
            return inputPath;
        }
        ProbeResult result = new ProbeResult();
        try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(inputPath)) {
            grabber.start();
            int w = grabber.getImageWidth();
            int h = grabber.getImageHeight();
            result.setWidth(w > 0 ? w : null);
            result.setHeight(h > 0 ? h : null);
            double fr = grabber.getFrameRate();
            result.setFrameRate(fr > 0 ? fr : null);
            long lengthInTime = grabber.getLengthInTime();
            if (lengthInTime > 0) result.setDurationMs(lengthInTime / 1000L);
            int vc = grabber.getVideoCodec();
            int ac = grabber.getAudioCodec();
            result.setVideoCodec(vc > 0 ? vc : null);
            result.setAudioCodec(ac > 0 ? ac : null);
            result.setHasVideo(vc > 0);
            result.setHasAudio(ac > 0);
            grabber.stop();
        }
        if (context != null) context.setProbeResult(context.getCurrentStepIndex(), result);
        return inputPath;
    }
}
