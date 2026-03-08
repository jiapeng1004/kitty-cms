package icu.jiapeng.kitty.transcoder.func.engine;

import icu.jiapeng.kitty.transcoder.api.ProbeResult;
import icu.jiapeng.kitty.transcoder.api.StrategyStepVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ProbeStepExecutor 单元测试。需要系统已安装 ffprobe，且 C:\Users\24291\Videos 下有测试视频。
 */
@EnabledOnOs({OS.WINDOWS, OS.LINUX})
class ProbeStepExecutorTest {

    private static final String TEST_VIDEO_DIR = "C:\\Users\\24291\\Videos";

    private ProbeStepExecutor executor;

    @BeforeEach
    void setUp() {
        executor = new ProbeStepExecutor(new FFmpegCliExecutor(), new FfprobeJsonParser());
    }

    private File findOneTestVideo() {
        File dir = new File(TEST_VIDEO_DIR);
        if (!dir.isDirectory()) return null;
        File[] files = dir.listFiles((d, name) -> {
            String lower = name.toLowerCase();
            return lower.endsWith(".mp4") || lower.endsWith(".mkv");
        });
        if (files == null || files.length == 0) return null;
        return files[0];
    }

    @Test
    void executeOnVideoFileReturnsProbeResult() throws Exception {
        File video = findOneTestVideo();
        if (video == null) {
            System.out.println("跳过: 无测试视频");
            return;
        }
        StepContextImpl ctx = new StepContextImpl((a, b) -> b);
        ctx.setCurrentStepIndex(0);
        String out = executor.execute(video.getAbsolutePath(), new StrategyStepVO(), "_s0", ctx);
        assertEquals(video.getAbsolutePath(), out);
        ProbeResult result = ctx.getProbeResult(0);
        assertNotNull(result);
        assertTrue(Boolean.TRUE.equals(result.getHasVideo()));
        assertNotNull(result.getWidth());
        assertNotNull(result.getHeight());
    }

    @Test
    void executeOnDirectoryReturnsInputPathAndEmptyProbe() throws Exception {
        File dir = new File(TEST_VIDEO_DIR);
        if (!dir.isDirectory()) return;
        StepContextImpl ctx = new StepContextImpl((a, b) -> b);
        ctx.setCurrentStepIndex(0);
        String out = executor.execute(dir.getAbsolutePath(), new StrategyStepVO(), "_s0", ctx);
        assertEquals(dir.getAbsolutePath(), out);
        ProbeResult result = ctx.getProbeResult(0);
        assertNotNull(result);
    }
}
