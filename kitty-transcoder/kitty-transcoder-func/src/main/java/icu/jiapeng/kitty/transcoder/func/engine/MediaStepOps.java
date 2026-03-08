package icu.jiapeng.kitty.transcoder.func.engine;

import cn.hutool.core.img.ImgUtil;
import cn.hutool.core.io.file.PathUtil;
import icu.jiapeng.kitty.transcoder.api.ProbeResult;
import icu.jiapeng.kitty.transcoder.api.StrategyStepVO;
import icu.jiapeng.kitty.transcoder.func.config.TranscodeConfig;
import icu.jiapeng.kitty.transcoder.func.file.HttpFileHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

@Slf4j
@Component
public class MediaStepOps {

    @Autowired(required = false)
    private TranscodeConfig transcodeConfig;

    private final FFmpegCliExecutor cliExecutor;
    private final FfprobeJsonParser ffprobeParser;

    public MediaStepOps(FFmpegCliExecutor cliExecutor, FfprobeJsonParser ffprobeParser) {
        this.cliExecutor = cliExecutor;
        this.ffprobeParser = ffprobeParser;
    }

    public String doTranscode(String inputPath, StrategyStepVO step, String stepSuffix, String resolvedOutputPath,
                              String taskWatermarkUrl, String taskWatermarkPosition, String taskId, String stepWorkDir,
                              StepContext context) throws Exception {
        File input = new File(inputPath);
        if (input.isDirectory()) throw new IOException("转码步骤需要文件输入，当前为目录");
        int width = 1920, height = 1080;
        if (step.getResolution() != null && !step.getResolution().isBlank()) {
            String[] parts = step.getResolution().split("[xX×]");
            if (parts.length >= 2) {
                try {
                    width = Integer.parseInt(parts[0].trim());
                    height = Integer.parseInt(parts[1].trim());
                } catch (NumberFormatException ignored) {
                }
            }
        }
        String format = step.getTargetFormat() != null ? step.getTargetFormat() : "mp4";
        String workDir = (stepWorkDir != null && !stepWorkDir.isBlank()) ? stepWorkDir : (transcodeConfig != null ? transcodeConfig.getWorkDir() : null);
        String outputFile = resolvedOutputPath != null && !resolvedOutputPath.isBlank()
                ? toLocalFilePath(resolvedOutputPath, workDir)
                : parentPath(inputPath) + File.separator + baseName(inputPath) + stepSuffix + "." + format;
        outputFile = ensureVideoExtension(outputFile, format);
        File outF = new File(outputFile);
        if (outF.getParent() != null) {
            File parent = new File(outF.getParent());
            if (!parent.exists()) parent.mkdirs();
        }
        int bitrate = (step.getBitrate() != null ? step.getBitrate() : 5000) * 1000;
        double frameRate = step.getFrameRate() != null ? step.getFrameRate().doubleValue() : 30;
        String codec = step.getEncoder() != null ? step.getEncoder() : "h264";

        long durationMs = 0;
        try {
            String json = cliExecutor.runFfprobe(Arrays.asList("-v", "quiet", "-print_format", "json", "-show_format", "-show_streams", inputPath), outF.getParentFile());
            ProbeResult probe = ffprobeParser.parse(json);
            if (probe.getDurationMs() != null) durationMs = probe.getDurationMs();
        } catch (Exception e) {
            log.debug("ffprobe duration 获取失败，进度不解析: {}", e.getMessage());
        }

        List<String> args = new ArrayList<>();
        args.add("-y");
        args.add("-i");
        args.add(inputPath);
        args.add("-s");
        args.add(width + "x" + height);
        args.add("-b:v");
        args.add(String.valueOf(bitrate));
        args.add("-r");
        args.add(String.valueOf(frameRate));
        args.add("-c:v");
        if ("h264".equalsIgnoreCase(codec) || "libx264".equalsIgnoreCase(codec)) {
            args.add("libx264");
        } else {
            args.add(codec);
        }
        args.add("-pix_fmt");
        args.add("yuv420p");
        if ("mp4".equalsIgnoreCase(format)) {
            args.add("-movflags");
            args.add("+faststart");
        }
        args.add("-c:a");
        args.add("aac");
        args.add("-b:a");
        args.add("128000");
        args.add("-ar");
        args.add("44100");
        args.add(outputFile);

        java.util.function.BooleanSupplier cancelled = context != null ? context::isCancelled : () -> false;
        Consumer<Integer> progress = (context != null && durationMs > 0) ? context::reportStepProgress : null;
        int exit = cliExecutor.runFfmpeg(args, outF.getParentFile(), 0, cancelled, progress, durationMs);
        if (exit != 0) {
            throw new IOException("ffmpeg 转码退出码: " + exit);
        }

        String wmPath = (taskWatermarkUrl != null && !taskWatermarkUrl.isBlank()) ? taskWatermarkUrl : null;
        if (wmPath != null) {
            if ((wmPath.startsWith("http://") || wmPath.startsWith("https://")) && taskId != null) {
                String downloadBase = (stepWorkDir != null && !stepWorkDir.isBlank()) ? stepWorkDir : (transcodeConfig != null ? transcodeConfig.getTempDir() : null);
                if (downloadBase != null)
                    wmPath = HttpFileHandler.downloadToTemp(wmPath, taskId + "_wm", downloadBase, "watermark_" + System.currentTimeMillis() + ".png");
            }
            File wmFile = new File(wmPath);
            if (wmFile.exists()) {
                String watermarked = outputFile + ".wmtmp";
                addWatermark(outputFile, watermarked, wmPath, taskWatermarkPosition);
                Files.delete(Paths.get(outputFile));
                Files.move(Paths.get(watermarked), Paths.get(outputFile), StandardCopyOption.REPLACE_EXISTING);
            }
        }
        return outputFile;
    }

    public String doExtractFrames(String inputPath, StrategyStepVO step, String stepSuffix, String resolvedOutputPath, String stepWorkDir) {
        try {
            return doExtractFrames(inputPath, step, stepSuffix, resolvedOutputPath, stepWorkDir, null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String doExtractFrames(String inputPath, StrategyStepVO step, String stepSuffix, String resolvedOutputPath, String stepWorkDir, StepContext context) throws Exception {
        String workDir = (stepWorkDir != null && !stepWorkDir.isBlank()) ? stepWorkDir : (transcodeConfig != null ? transcodeConfig.getWorkDir() : null);
        String framePath = resolvedOutputPath != null && !resolvedOutputPath.isBlank()
                ? toLocalFilePath(resolvedOutputPath, workDir)
                : parentPath(inputPath) + File.separator + baseName(inputPath) + stepSuffix;
        int interval = step.getFrameInterval() != null && step.getFrameInterval() > 0 ? step.getFrameInterval() : 30;
        int frameCount = step.getExtractFrameCount() != null && step.getExtractFrameCount() > 0 ? step.getExtractFrameCount() : 1;
        String fmt = "jpg";
        if (step.getExtractOutputFormat() != null && step.getExtractOutputFormat().equalsIgnoreCase("png")) fmt = "png";

        Path framePathO = Paths.get(framePath);
        PathUtil.mkParentDirs(framePathO);
        File framePathFile = new File(framePath);

        if (frameCount == 1) {
            String outFile = framePath;
            if (!outFile.toLowerCase().endsWith("." + fmt)) {
                outFile = framePath + "." + fmt;
            }
            List<String> args = Arrays.asList(
                    "-y", "-i", inputPath,
                    "-vf", "select=eq(n\\,0)", "-vframes", "1",
                    outFile
            );
            java.util.function.BooleanSupplier cancelled = context != null ? context::isCancelled : () -> false;
            int exit = cliExecutor.runFfmpeg(args, framePathFile.getParentFile(), 0, cancelled, null, 0);
            if (exit != 0) throw new IOException("ffmpeg 抽帧退出码: " + exit);
            return new File(outFile).getAbsolutePath();
        }

        if (framePathFile.exists() && framePathFile.isFile()) {
            Files.deleteIfExists(framePathO);
        }
        Files.createDirectories(framePathO);
        String outPattern = framePath + File.separator + "frame_%d." + fmt;
        List<String> args = Arrays.asList(
                "-y", "-i", inputPath,
                "-vf", "select=not(mod(n\\," + interval + "))", "-vsync", "vfr",
                "-frame_pts", "1", "-start_number", "0",
                outPattern
        );
        java.util.function.BooleanSupplier cancelled = context != null ? context::isCancelled : () -> false;
        int exit = cliExecutor.runFfmpeg(args, framePathFile.getParentFile(), 0, cancelled, null, 0);
        if (exit != 0) throw new IOException("ffmpeg 抽帧退出码: " + exit);
        return framePathFile.getAbsolutePath();
    }

    public String doSpriteSheet(String inputPath, StrategyStepVO step, String stepSuffix, String resolvedOutputPath, String stepWorkDir) throws Exception {
        File input = new File(inputPath);
        int cols = step.getSpriteColumns() != null && step.getSpriteColumns() > 0 ? step.getSpriteColumns() : 4;
        int rows = step.getSpriteRows() != null && step.getSpriteRows() > 0 ? step.getSpriteRows() : 3;
        int count = cols * rows;
        int scale = step.getSpriteScale() != null && step.getSpriteScale() > 0 ? step.getSpriteScale() : 4;
        String fmt = "jpg";
        if (step.getExtractOutputFormat() != null && step.getExtractOutputFormat().equalsIgnoreCase("png")) fmt = "png";
        BufferedImage sprite = null;
        if (input.isDirectory()) {
            List<BufferedImage> images = loadFramesFromDirectory(inputPath, count);
            if (!images.isEmpty()) sprite = buildSpriteFromImages(images, cols, rows, scale);
        } else {
            sprite = buildSpriteWithFfmpeg(inputPath, cols, rows, count, scale);
            if (sprite == null) {
                String tempDir = parentPath(inputPath) + File.separator + ".sprite_tmp_" + System.currentTimeMillis();
                Files.createDirectories(Paths.get(tempDir));
                try {
                    String multiOut = doExtractFrames(inputPath, step, "_sprite_tmp", tempDir, stepWorkDir, null);
                    File dir = new File(multiOut);
                    if (dir.isDirectory()) {
                        List<BufferedImage> images = loadFramesFromDirectory(multiOut, count);
                        if (!images.isEmpty()) sprite = buildSpriteFromImages(images, cols, rows, scale);
                    }
                } finally {
                    PathUtil.del(Paths.get(tempDir));
                }
            }
        }
        if (sprite == null) {
            throw new IOException("未抽到帧或雪碧图生成失败");
        }
        String baseName = baseName(inputPath);
        String workDir = (stepWorkDir != null && !stepWorkDir.isBlank()) ? stepWorkDir : (transcodeConfig != null ? transcodeConfig.getWorkDir() : null);
        String outPath = resolvedOutputPath != null && !resolvedOutputPath.isBlank()
                ? toLocalFilePath(resolvedOutputPath, workDir)
                : parentPath(inputPath) + File.separator + baseName + stepSuffix + "_sprite." + fmt;
        outPath = ensureImageExtension(outPath, fmt);
        File outFile = new File(outPath);
        if (outFile.getParent() != null) {
            File parent = new File(outFile.getParent());
            if (!parent.exists()) parent.mkdirs();
        }
        ImgUtil.write(sprite, outFile);
        return outPath;
    }

    private BufferedImage buildSpriteWithFfmpeg(String inputPath, int cols, int rows, int count, int scale) throws Exception {
        String json = cliExecutor.runFfprobe(Arrays.asList("-v", "quiet", "-print_format", "json", "-show_format", "-show_streams", inputPath), new File(inputPath).getParentFile());
        ProbeResult probe = ffprobeParser.parse(json);
        Integer w = probe.getWidth();
        Integer h = probe.getHeight();
        if (w == null || h == null || w <= 0 || h <= 0) return null;
        long durationMs = probe.getDurationMs() != null ? probe.getDurationMs() : 10000;
        Double fps = probe.getFrameRate();
        double fpsVal = (fps != null && fps > 0) ? fps : 30;
        long totalFrames = (long) (durationMs / 1000.0 * fpsVal);
        int interval = Math.max(1, (int) (totalFrames / count));
        int s = Math.max(1, scale);
        String filter = "select=not(mod(n\\," + interval + ")),scale=iw/" + s + ":ih/" + s + ",tile=" + cols + "x" + rows;
        File parent = new File(inputPath).getParentFile();
        File outImg = File.createTempFile("sprite_", "." + "jpg", parent);
        try {
            List<String> args = Arrays.asList(
                    "-y", "-i", inputPath,
                    "-vf", filter, "-frames:v", "1",
                    outImg.getAbsolutePath()
            );
            int exit = cliExecutor.runFfmpeg(args, parent, 0, null, null, 0);
            if (exit != 0) return null;
            return ImageIO.read(outImg);
        } finally {
            Files.deleteIfExists(outImg.toPath());
        }
    }

    private BufferedImage buildSpriteFromImages(List<BufferedImage> images, int cols, int rows, int scale) {
        int maxW = 0, maxH = 0;
        for (BufferedImage img : images) {
            if (img != null) {
                maxW = Math.max(maxW, img.getWidth());
                maxH = Math.max(maxH, img.getHeight());
            }
        }
        int s = Math.max(1, scale);
        int cellW = maxW / s;
        int cellH = maxH / s;
        BufferedImage sprite = new BufferedImage(cellW * cols, cellH * rows, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = sprite.createGraphics();
        for (int i = 0; i < images.size(); i++) {
            BufferedImage img = images.get(i);
            if (img != null) {
                int r = i / cols, c = i % cols;
                g.drawImage(img, c * cellW, r * cellH, cellW, cellH, null);
            }
        }
        g.dispose();
        return sprite;
    }

    private List<BufferedImage> loadFramesFromDirectory(String dirPath, int count) throws IOException {
        List<BufferedImage> list = new ArrayList<>();
        File dir = new File(dirPath);
        if (!dir.isDirectory()) return list;
        File[] files = dir.listFiles((d, name) -> {
            String lower = name.toLowerCase();
            return lower.matches("frame_\\d+\\.(png|jpg|jpeg|webp|bmp)");
        });
        if (files == null || files.length == 0) return list;
        java.util.Arrays.sort(files, java.util.Comparator.comparing(File::getName));
        int n = Math.min(count, files.length);
        for (int i = 0; i < n; i++) {
            try {
                BufferedImage img = ImageIO.read(files[i]);
                if (img != null) list.add(img);
            } catch (IOException e) {
                log.warn("加载帧失败: {}", files[i].getAbsolutePath(), e);
            }
        }
        return list;
    }

    public String doImageConvert(String inputPath, StrategyStepVO step, String stepSuffix, String resolvedOutputPath, String stepWorkDir) throws Exception {
        File input = new File(inputPath);
        String workDir = (stepWorkDir != null && !stepWorkDir.isBlank()) ? stepWorkDir : (transcodeConfig != null ? transcodeConfig.getWorkDir() : null);
        String targetFormat = step.getImageTargetFormat() != null && !step.getImageTargetFormat().isBlank()
                ? step.getImageTargetFormat().toLowerCase() : "webp";
        int quality = step.getImageQuality() != null ? Math.min(100, Math.max(1, step.getImageQuality())) : 85;
        String resize = step.getImageResize() != null && !step.getImageResize().isBlank() ? step.getImageResize().trim() : null;

        if (input.isFile()) {
            String outPath = resolvedOutputPath != null && !resolvedOutputPath.isBlank()
                    ? toLocalFilePath(resolvedOutputPath, workDir)
                    : parentPath(inputPath) + File.separator + baseName(inputPath) + stepSuffix + "." + targetFormat;
            File outFile = new File(outPath);
            if (outFile.getParent() != null) {
                File parent = new File(outFile.getParent());
                if (!parent.exists()) parent.mkdirs();
            }
            runImageMagickConvert(inputPath, outPath, targetFormat, quality, resize);
            return outPath;
        }

        if (input.isDirectory()) {
            String outDir = resolvedOutputPath != null && !resolvedOutputPath.isBlank()
                    ? toLocalFilePath(resolvedOutputPath, workDir)
                    : parentPath(inputPath) + File.separator + baseName(inputPath) + stepSuffix + "_converted";
            File outDirF = new File(outDir);
            if (!outDirF.exists()) outDirF.mkdirs();
            File[] files = input.listFiles((dir, name) -> {
                String lower = name.toLowerCase();
                return lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png")
                        || lower.endsWith(".webp") || lower.endsWith(".gif") || lower.endsWith(".bmp")
                        || lower.endsWith(".tiff") || lower.endsWith(".tif");
            });
            if (files == null || files.length == 0) throw new IOException("目录内无支持的图片文件");
            for (File f : files) {
                String base = baseName(f.getAbsolutePath());
                String outPath = outDir + File.separator + base + "." + targetFormat;
                runImageMagickConvert(f.getAbsolutePath(), outPath, targetFormat, quality, resize);
            }
            return outDir;
        }

        throw new IOException("输入路径既不是文件也不是目录: " + inputPath);
    }

    private void runImageMagickConvert(String inputPath, String outputPath, String targetFormat, int quality, String resize) throws Exception {
        List<String> args = new ArrayList<>();
        args.add(inputPath);
        if (resize != null && !resize.isBlank()) {
            args.add("-resize");
            args.add(resize);
        }
        if ("jpg".equals(targetFormat) || "jpeg".equals(targetFormat) || "webp".equals(targetFormat)) {
            args.add("-quality");
            args.add(String.valueOf(quality));
        }
        args.add(outputPath);
        List<String> cmd = new ArrayList<>();
        cmd.add("magick");
        cmd.addAll(args);
        try {
            Process p = new ProcessBuilder(cmd).inheritIO().start();
            if (p.waitFor() == 0) return;
        } catch (Exception e) {
            log.error("ImageMagick 执行失败", e);
            throw new IOException("ImageMagick 执行失败，" + e.getMessage());
        }
        throw new IOException("ImageMagick 执行失败，请确保已安装 ImageMagick (magick命令)");
    }

    public void addWatermark(String inputFile, String outputFile, String watermarkPath, String position) throws Exception {
        if (watermarkPath == null || watermarkPath.isBlank() || !new File(watermarkPath).exists()) {
            Files.copy(new File(inputFile).toPath(), new File(outputFile).toPath(), StandardCopyOption.REPLACE_EXISTING);
            return;
        }
        String overlay = "W-w-10:H-h-10";
        if (position != null) {
            switch (position.toLowerCase()) {
                case "top-left":
                    overlay = "10:10";
                    break;
                case "top-right":
                    overlay = "W-w-10:10";
                    break;
                case "bottom-left":
                    overlay = "10:H-h-10";
                    break;
                default:
                    overlay = "W-w-10:H-h-10";
            }
        }
        ProcessBuilder pb = new ProcessBuilder("ffmpeg", "-y", "-i", inputFile, "-i", watermarkPath,
                "-filter_complex", "[1]scale=iw/4:-1[wm];[0][wm]overlay=" + overlay, "-c:a", "copy", outputFile);
        pb.inheritIO();
        Process p = pb.start();
        if (p.waitFor() != 0) throw new IOException("ffmpeg watermark failed");
    }

    public static String baseName(String path) {
        String name = new File(path).getName();
        int dot = name.lastIndexOf('.');
        return dot > 0 ? name.substring(0, dot) : name;
    }

    public static String parentPath(String path) {
        return new File(path).getParent();
    }

    private static String ensureVideoExtension(String path, String videoFmt) {
        if (path == null || path.isBlank() || videoFmt == null || videoFmt.isBlank()) return path;
        String lower = path.toLowerCase();
        if (lower.endsWith(".png") || lower.endsWith(".jpg") || lower.endsWith(".jpeg")
                || lower.endsWith(".webp") || lower.endsWith(".gif") || lower.endsWith(".bmp")) {
            int lastDot = path.lastIndexOf('.');
            return (lastDot > 0 ? path.substring(0, lastDot) : path) + "." + videoFmt.replace(".", "");
        }
        return path;
    }

    private static String ensureImageExtension(String path, String imageFmt) {
        if (path == null || path.isBlank()) return path;
        String lower = path.toLowerCase();
        if (lower.endsWith(".mp4") || lower.endsWith(".mpg") || lower.endsWith(".mpeg")
                || lower.endsWith(".avi") || lower.endsWith(".mov") || lower.endsWith(".mkv")
                || lower.endsWith(".webm") || lower.endsWith(".flv")) {
            int lastDot = path.lastIndexOf('.');
            return (lastDot > 0 ? path.substring(0, lastDot) : path) + "." + imageFmt;
        }
        return path;
    }

    public static String toLocalFilePath(String path, String workDir) {
        if (path == null || path.isBlank()) return path;
        String s = path.trim();
        int q = s.indexOf('?');
        if (q >= 0) s = s.substring(0, q) + "_" + s.substring(q + 1).replaceAll("[?&#]", "_");
        if (workDir != null && !workDir.isBlank()) {
            Path p = Paths.get(s);
            if (!p.isAbsolute()) {
                s = Paths.get(workDir).resolve(p).normalize().toAbsolutePath().toString();
            }
        }
        return s;
    }
}
