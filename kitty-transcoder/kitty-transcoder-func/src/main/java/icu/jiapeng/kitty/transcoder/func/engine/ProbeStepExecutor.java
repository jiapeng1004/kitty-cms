package icu.jiapeng.kitty.transcoder.func.engine;

import icu.jiapeng.kitty.transcoder.api.ProbeResult;
import icu.jiapeng.kitty.transcoder.api.StrategyStepVO;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Arrays;
import java.util.List;

@Component
public class ProbeStepExecutor implements StepExecutor {

    private final FFmpegCliExecutor cliExecutor;
    private final FfprobeJsonParser ffprobeParser;

    public ProbeStepExecutor(FFmpegCliExecutor cliExecutor, FfprobeJsonParser ffprobeParser) {
        this.cliExecutor = cliExecutor;
        this.ffprobeParser = ffprobeParser;
    }

    @Override
    public String getType() {
        return StepExecutorType.PROBE.getCode();
    }

    @Override
    public String execute(String inputPath,String outputPath, StrategyStepVO step) throws Exception {
        StepContext context = StepContext.getInstance();
        File input = new File(inputPath);
        if (!input.exists()) throw new java.io.IOException("输入不存在：" + inputPath);
        if (input.isDirectory()) {
            if (context != null) context.setProbeResult(context.getCurrentStepIndex(), new ProbeResult());
            return inputPath;
        }
        List<String> args = Arrays.asList(
                "-v", "quiet",
                "-print_format", "json",
                "-show_format", "-show_streams",
                inputPath
        );
        File workDir = input.getParentFile();
        String json = cliExecutor.runFfprobe(args, workDir);
        ProbeResult result = ffprobeParser.parse(json);
        if (context != null) context.setProbeResult(context.getCurrentStepIndex(), result);
        return inputPath;
    }
}
