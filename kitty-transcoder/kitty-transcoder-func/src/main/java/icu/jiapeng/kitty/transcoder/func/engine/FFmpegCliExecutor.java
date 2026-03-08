package icu.jiapeng.kitty.transcoder.func.engine;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

/**
 * FFmpeg / ffprobe 命令行封装：启动进程、超时与取消。
 * 进度通过 ffmpeg -progress pipe:1 从 stdout 读取（out_time_ms），比解析 stderr 更稳定、性能更好。
 */
@Slf4j
@Component
public class FFmpegCliExecutor {

    private static final int PROGRESS_REPORT_INTERVAL_MS = 300;
    /** -progress 输出中的 out_time_ms（单位：微秒） */
    private static final String PROGRESS_KEY_TIME_MS = "out_time_ms";

    @Value("${transcode.ffmpeg.path:ffmpeg}")
    private String ffmpegPath = "ffmpeg";

    @Value("${transcode.ffprobe.path:ffprobe}")
    private String ffprobePath = "ffprobe";

    /**
     * 执行 ffmpeg，可选的进度回调与取消检测。
     * 当需要进度时使用 -progress pipe:1，从 stdout 解析 out_time_ms（微秒），避免解析 stderr，性能更好。
     *
     * @param args          ffmpeg 参数（不含可执行路径），如 "-y", "-i", "in.mp4", "-c:v", "libx264", "out.mp4"
     * @param workDir       工作目录，可为 null
     * @param timeoutMs     超时毫秒，≤0 表示不超时
     * @param cancelled     取消检测，可为 null
     * @param progressPct   ［可选］进度 0–100 回调，约 300ms 节流；需配合 durationMs
     * @param durationMs    总时长毫秒，用于计算百分比；≤0 则不启用 -progress
     * @return 进程退出码，0 为成功
     */
    public int runFfmpeg(List<String> args, File workDir, long timeoutMs,
                        BooleanSupplier cancelled, Consumer<Integer> progressPct, long durationMs) throws IOException, InterruptedException {
        boolean useProgress = progressPct != null && durationMs > 0;
        List<String> cmd = new ArrayList<>();
        cmd.add(ffmpegPath);
        if (useProgress) {
            cmd.add("-progress");
            cmd.add("pipe:1");
        }
        cmd.addAll(args);
        ProcessBuilder pb = new ProcessBuilder(cmd);
        if (workDir != null && workDir.isDirectory()) {
            pb.directory(workDir);
        }
        pb.redirectErrorStream(false);
        Process p = pb.start();

        Thread stdoutReader = null;
        Thread stderrReader;
        if (useProgress) {
            long durationUs = durationMs * 1000L;
            long[] lastReport = {0};
            int[] lastPct = {-1};
            stdoutReader = new Thread(() -> {
                try (BufferedReader r = new BufferedReader(
                        new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = r.readLine()) != null) {
                        if (cancelled != null && cancelled.getAsBoolean()) {
                            p.destroyForcibly();
                            break;
                        }
                        if (line.startsWith(PROGRESS_KEY_TIME_MS + "=")) {
                            String val = line.substring(PROGRESS_KEY_TIME_MS.length() + 1).trim();
                            long outTimeUs = parseLongOrDefault(val, 0L);
                            int pct = (int) Math.min(99, Math.max(0, (int) (outTimeUs * 100 / durationUs)));
                            long now = System.currentTimeMillis();
                            if (pct != lastPct[0] && now - lastReport[0] >= PROGRESS_REPORT_INTERVAL_MS) {
                                lastReport[0] = now;
                                lastPct[0] = pct;
                                progressPct.accept(pct);
                            }
                        }
                    }
                } catch (IOException e) {
                    log.debug("ffmpeg progress read: {}", e.getMessage());
                }
            }, "ffmpeg-progress");
            stdoutReader.start();
        }
        stderrReader = new Thread(() -> {
            try (BufferedReader r = new BufferedReader(
                    new InputStreamReader(p.getErrorStream(), StandardCharsets.UTF_8))) {
                while (r.readLine() != null) {
                    if (cancelled != null && cancelled.getAsBoolean()) {
                        p.destroyForcibly();
                        break;
                    }
                }
            } catch (IOException e) {
                log.debug("ffmpeg stderr: {}", e.getMessage());
            }
        }, "ffmpeg-stderr");
        stderrReader.start();

        long deadline = (timeoutMs > 0) ? System.currentTimeMillis() + timeoutMs : Long.MAX_VALUE;
        while (true) {
            if (cancelled != null && cancelled.getAsBoolean()) {
                p.destroyForcibly();
                p.waitFor(5, TimeUnit.SECONDS);
                return -1;
            }
            if (System.currentTimeMillis() > deadline) {
                p.destroyForcibly();
                p.waitFor(5, TimeUnit.SECONDS);
                throw new IOException("ffmpeg 执行超时");
            }
            try {
                if (p.waitFor(500, TimeUnit.MILLISECONDS)) {
                    break;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                p.destroyForcibly();
                throw e;
            }
        }
        if (stdoutReader != null) {
            stdoutReader.join(2000);
        }
        stderrReader.join(2000);
        int exit = p.exitValue();
        if (progressPct != null && exit == 0) {
            progressPct.accept(100);
        }
        return exit;
    }

    private static long parseLongOrDefault(String s, long def) {
        if (s == null || s.isBlank()) return def;
        try {
            return Long.parseLong(s.trim());
        } catch (NumberFormatException e) {
            return def;
        }
    }

    /**
     * 执行 ffprobe，返回 stdout（通常为 JSON）。
     */
    public String runFfprobe(List<String> args, File workDir) throws IOException, InterruptedException {
        List<String> cmd = new ArrayList<>();
        cmd.add(ffprobePath);
        cmd.addAll(args);
        ProcessBuilder pb = new ProcessBuilder(cmd);
        if (workDir != null && workDir.isDirectory()) {
            pb.directory(workDir);
        }
        pb.redirectErrorStream(true);
        Process p = pb.start();
        StringBuilder out = new StringBuilder();
        try (BufferedReader r = new BufferedReader(
                new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = r.readLine()) != null) {
                out.append(line).append('\n');
            }
        }
        boolean ok = p.waitFor(30, TimeUnit.SECONDS);
        if (!ok) {
            p.destroyForcibly();
            throw new IOException("ffprobe 执行超时");
        }
        if (p.exitValue() != 0) {
            throw new IOException("ffprobe 退出码: " + p.exitValue() + ", 输出: " + out);
        }
        return out.toString();
    }

    public String getFfmpegPath() {
        return ffmpegPath;
    }

    public String getFfprobePath() {
        return ffprobePath;
    }
}
