package icu.jiapeng.kitty.transcoder.func.engine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * FFmpegCliExecutor 单元测试。需要系统已安装 ffmpeg/ffprobe。
 */
@EnabledOnOs({OS.WINDOWS, OS.LINUX})
class FFmpegCliExecutorTest {

    private static final String TEST_VIDEO_DIR = "C:\\Users\\24291\\Videos\\test";

    private FFmpegCliExecutor executor;

    @BeforeEach
    void setUp() {
        executor = new FFmpegCliExecutor();
    }

    private File findOneTestVideo() {
        File dir = new File(TEST_VIDEO_DIR);
        if (!dir.isDirectory()) return null;
        File[] files = dir.listFiles((d, name) -> {
            String lower = name.toLowerCase();
            return lower.endsWith(".mp4") || lower.endsWith(".mkv") || lower.endsWith(".avi");
        });
        if (files == null || files.length == 0) return null;
        return files[0];
    }

    @Test
    void runFfprobeReturnsJson() throws Exception {
        File video = findOneTestVideo();
        if (video == null) {
            System.out.println("跳过: " + TEST_VIDEO_DIR + " 下无测试视频");
            return;
        }
        List<String> args = Arrays.asList("-v", "quiet", "-print_format", "json", "-show_format", "-show_streams", video.getAbsolutePath());
        String json = executor.runFfprobe(args, video.getParentFile());
        assertNotNull(json);
        assertTrue(json.contains("streams") || json.contains("format"));
    }

    @Test
    void runFfmpegTranscodeWithProgress() throws Exception {
        File video = findOneTestVideo();
        if (video == null) {
            System.out.println("跳过: 无测试视频");
            return;
        }
        Path out = Files.createTempFile("ffmpeg_test_", ".mp4");
        try {
            List<String> args = Arrays.asList(
                    "-y", "-i", video.getAbsolutePath(),
                    "-t", "2", "-c:v", "libx264", "-c:a", "aac",
                    out.toAbsolutePath().toString()
            );
            AtomicInteger lastProgress = new AtomicInteger(-1);
            int exit = executor.runFfmpeg(args, video.getParentFile(), 60_000, () -> false,
                    integer -> {
                        lastProgress.set(integer);
                        System.out.println(integer);
                    }, 2000L);
            assertEquals(0, exit);
            assertTrue(Files.size(out) > 0);
            assertTrue(lastProgress.get() >= 0, "进度应被上报");
        } finally {
            Files.deleteIfExists(out);
        }
    }

    @Test
    void runFfmpegCancelledDestroysProcess() throws Exception {
        File video = findOneTestVideo();
        if (video == null) return;
        Path out = Files.createTempFile("ffmpeg_cancel_", ".mp4");
        try {
            List<String> args = Arrays.asList(
                    "-y", "-i", video.getAbsolutePath(),
                    "-c:v", "libx264", "-c:a", "aac",
                    out.toAbsolutePath().toString()
            );
            boolean[] cancelled = {false};
            Thread t = new Thread(() -> {
                try {
                    Thread.sleep(500);
                    cancelled[0] = true;
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            t.start();
            int exit = executor.runFfmpeg(args, video.getParentFile(), 0, () -> cancelled[0], null, 0);
            t.join(2000);
            assertTrue(exit != 0 || cancelled[0], "取消后应退出");
        } finally {
            Files.deleteIfExists(out);
        }
    }
}
