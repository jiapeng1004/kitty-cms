package icu.jiapeng.kitty.transcoder.func.engine;

import icu.jiapeng.kitty.transcoder.api.StrategyStepVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * MediaStepOps 单元测试。需要 ffmpeg/ffprobe，且 C:\Users\24291\Videos 下有测试视频。
 */
@EnabledOnOs({OS.WINDOWS, OS.LINUX})
class MediaStepOpsTest {

    private static final String TEST_VIDEO_DIR = "C:\\Users\\24291\\Videos";

    private MediaStepOps mediaStepOps;

    @BeforeEach
    void setUp() {
        mediaStepOps = new MediaStepOps(new FFmpegCliExecutor(), new FfprobeJsonParser());
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
    void doExtractFramesSingleFrame() throws Exception {
        File video = findOneTestVideo();
        if (video == null) return;
        Path outDir = Files.createTempDirectory("extract_single_");
        try {
            StrategyStepVO step = new StrategyStepVO();
            step.setFrameInterval(30);
            step.setExtractFrameCount(1);
            step.setExtractOutputFormat("jpg");
            String out = mediaStepOps.doExtractFrames(video.getAbsolutePath(), step, "_test", outDir.toString(), TEST_VIDEO_DIR, null);
            assertNotNull(out);
            File f = new File(out);
            assertTrue(f.exists());
            assertTrue(f.length() > 0);
        } finally {
            deleteRecursive(outDir.toFile());
        }
    }

    @Test
    void doExtractFramesMultiFrame() throws Exception {
        File video = findOneTestVideo();
        if (video == null) return;
        Path outDir = Files.createTempDirectory("extract_multi_");
        try {
            StrategyStepVO step = new StrategyStepVO();
            step.setFrameInterval(30);
            step.setExtractFrameCount(5);
            step.setExtractOutputFormat("jpg");
            String out = mediaStepOps.doExtractFrames(video.getAbsolutePath(), step, "_test", outDir.toString(), TEST_VIDEO_DIR, null);
            assertNotNull(out);
            File dir = new File(out);
            assertTrue(dir.isDirectory());
            File[] frames = dir.listFiles((d, name) -> name.matches("frame_\\d+\\.jpg"));
            assertNotNull(frames);
            assertTrue(frames.length >= 1);
        } finally {
            deleteRecursive(outDir.toFile());
        }
    }

    @Test
    void doTranscodeShortSegment() throws Exception {
        File video = findOneTestVideo();
        if (video == null) return;
        Path outFile = Files.createTempFile("transcode_test_", ".mp4");
        try {
            StrategyStepVO step = new StrategyStepVO();
            step.setResolution("640x360");
            step.setBitrate(1000);
            step.setFrameRate(25);
            step.setTargetFormat("mp4");
            step.setEncoder("libx264");
            String out = mediaStepOps.doTranscode(video.getAbsolutePath(), step, "_test", outFile.toString(), null, null, null, TEST_VIDEO_DIR, null);
            assertNotNull(out);
            assertTrue(Files.size(outFile) > 0);
        } finally {
            Files.deleteIfExists(outFile);
        }
    }

    @Test
    void doSpriteSheetFromVideo() throws Exception {
        File video = findOneTestVideo();
        if (video == null) return;
        Path outFile = Files.createTempFile("sprite_test_", ".jpg");
        try {
            StrategyStepVO step = new StrategyStepVO();
            step.setSpriteColumns(2);
            step.setSpriteRows(2);
            step.setSpriteScale(4);
            step.setExtractOutputFormat("jpg");
            String out = mediaStepOps.doSpriteSheet(video.getAbsolutePath(), step, "_test", outFile.toString(), TEST_VIDEO_DIR);
            assertNotNull(out);
            assertTrue(new File(out).exists());
            assertTrue(Files.size(outFile) > 0);
        } finally {
            Files.deleteIfExists(outFile);
        }
    }

    @Test
    void toLocalFilePathResolvesRelative() {
        String abs = MediaStepOps.toLocalFilePath("sub/out.mp4", "C:\\work");
        assertNotNull(abs);
        assertTrue(abs.contains("work"));
        assertTrue(abs.replace("/", "\\").contains("sub"));
    }

    @Test
    void baseNameAndParentPath() {
        assertEquals("video", MediaStepOps.baseName("C:\\dir\\video.mp4"));
        assertTrue(MediaStepOps.parentPath("C:\\dir\\video.mp4").replace("/", "\\").contains("dir"));
    }

    private static void deleteRecursive(File f) {
        if (f.isDirectory()) {
            File[] children = f.listFiles();
            if (children != null) for (File c : children) deleteRecursive(c);
        }
        f.delete();
    }
}
