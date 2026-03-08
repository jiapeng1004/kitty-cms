package icu.jiapeng.kitty.transcoder.func.engine;

import cn.hutool.core.img.ImgUtil;
import cn.hutool.core.io.file.PathUtil;
import icu.jiapeng.kitty.transcoder.api.StrategyStepVO;
import icu.jiapeng.kitty.transcoder.func.config.TranscodeConfig;
import icu.jiapeng.kitty.transcoder.func.file.HttpFileHandler;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.javacv.FFmpegFrameFilter;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
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
import java.util.List;

import static org.bytedeco.ffmpeg.global.avcodec.AV_CODEC_ID_H264;
import static org.bytedeco.ffmpeg.global.avutil.AV_PIX_FMT_YUV420P;

@Slf4j
@Component
public class MediaStepOps {

    @Autowired(required = false)
    private TranscodeConfig transcodeConfig;

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
        File outF = new File(outputFile);
        if (outF.getParent() != null) {
            File parent = new File(outF.getParent());
            if (!parent.exists()) parent.mkdirs();
        }
        int bitrate = (step.getBitrate() != null ? step.getBitrate() : 5000) * 1000;
        double frameRate = step.getFrameRate() != null ? step.getFrameRate() : 30;
        String codec = step.getEncoder() != null ? step.getEncoder() : "h264";

        try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(inputPath);
             FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputFile, width, height, 2)) {
            grabber.start();
            long totalMicros = grabber.getLengthInTime();
            long lastReportMs = 0;
            recorder.setFormat(format);
            if ("h264".equalsIgnoreCase(codec) || "libx264".equalsIgnoreCase(codec)) {
                recorder.setVideoCodec(AV_CODEC_ID_H264);
            } else {
                recorder.setVideoCodecName(codec);
            }
            recorder.setVideoBitrate(bitrate);
            recorder.setFrameRate(frameRate);
            recorder.setPixelFormat(AV_PIX_FMT_YUV420P);
            if ("mp4".equalsIgnoreCase(format)) {
                recorder.setOption("movflags", "+faststart");
            }
            recorder.setAudioCodecName("aac");
            recorder.setAudioBitrate(128000);
            recorder.setSampleRate(44100);
            recorder.start();
            Frame frame;
            while ((frame = grabber.grab()) != null) {
                if (context != null && context.isCancelled()) {
                    throw new IOException("任务已取消");
                }
                recorder.record(frame);
                if (context != null && totalMicros > 0) {
                    long now = System.currentTimeMillis();
                    if (now - lastReportMs >= 300) {
                        lastReportMs = now;
                        long pos = grabber.getTimestamp();
                        int pct = (int) Math.min(99, Math.max(0, (pos * 100) / totalMicros));
                        context.reportStepProgress(pct);
                    }
                }
            }
            if (context != null) context.reportStepProgress(100);
            recorder.stop();
            grabber.stop();
        }
        // 任务级水印：发起转码时传入才加水印（支持本地路径或 HTTP URL）
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

    @SneakyThrows
    public String doExtractFrames(String inputPath, StrategyStepVO step, String stepSuffix, String resolvedOutputPath, String stepWorkDir) {
        return doExtractFrames(inputPath, step, stepSuffix, resolvedOutputPath, stepWorkDir, null);
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
        java.util.function.BooleanSupplier isCancelled = context != null ? context::isCancelled : () -> false;
        List<BufferedImage> frames = extractFramesByInterval(inputPath, interval, frameCount, isCancelled);
        Path framePathO = Paths.get(framePath);
        PathUtil.mkParentDirs(framePathO);
        if (frameCount == 1 && !frames.isEmpty()) {
            // 单帧：输出为单个文件 transcoder/$DATE/$TASK_ID_frame.png
            File outFile = new File(framePath);
            ImageIO.write(frames.getFirst(), fmt, outFile);
            return outFile.getAbsolutePath();
        }
        // 多帧：输出 transcoder/$DATE/$TASK_ID_frame_0.png, _1.png, ...
        File frameFile = new File(framePath);
        for (int i = 0; i < frames.size(); i++) {
            if (frameFile.exists() && frameFile.isFile()) {
                Files.deleteIfExists(framePathO);
            }
            if (!frameFile.exists()) {
                Files.createDirectories(framePathO);
            }
            BufferedImage img = frames.get(i);
            if (img != null) {
                File outFile = new File(frameFile, String.format("%d.%s", i, fmt));
                ImageIO.write(img, fmt, outFile);
            }
        }
        return frameFile.getAbsolutePath();
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
            sprite = buildSpriteWithFilter(inputPath, cols, rows, count, scale);
            if (sprite == null) {
                List<BufferedImage> images = extractFramesByTimestamp(inputPath, count);
                if (images.isEmpty()) images = extractFramesByInterval(inputPath, 30, count, null);
                if (!images.isEmpty()) sprite = buildSpriteFromImages(images, cols, rows, scale);
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
        // 雪碧图本质是静态图片，若 output_template 误填 .mp4 等视频扩展名，强制改为图片格式
        outPath = ensureImageExtension(outPath, fmt);
        File outFile = new File(outPath);
        if (outFile.getParent() != null) {
            File parent = new File(outFile.getParent());
            if (!parent.exists()) parent.mkdirs();
        }
        ImgUtil.write(sprite, outFile);
        return outPath;
    }

    /**
     * 使用 FFmpegFrameFilter 一步到位生成雪碧图：select+scale(iw/N:ih/N)+tile，无需先抽帧。
     */
    private BufferedImage buildSpriteWithFilter(String inputPath, int cols, int rows, int count, int scale) throws Exception {
        try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(inputPath);
             Java2DFrameConverter converter = new Java2DFrameConverter()) {
            grabber.start();
            int w = grabber.getImageWidth();
            int h = grabber.getImageHeight();
            if (w <= 0 || h <= 0) return null;
            long durationUs = grabber.getLengthInTime();
            double fps = grabber.getFrameRate();
            long totalFrames = (durationUs > 0 && fps > 0) ? (long) ((durationUs / 1e6) * fps) : 300;
            int interval = Math.max(1, (int) (totalFrames / count));
            int s = Math.max(1, scale);
            String filterStr = "select=not(mod(n\\," + interval + ")),scale=iw/" + s + ":ih/" + s + ",tile=" + cols + "x" + rows;
            try (FFmpegFrameFilter filter = new FFmpegFrameFilter(filterStr, w, h)) {
                int pf = grabber.getPixelFormat();
                if (pf >= 0) filter.setPixelFormat(pf);
                filter.setFrameRate(fps > 0 ? fps : 30);
                filter.start();
                Frame frame;
                while ((frame = grabber.grab()) != null) {
                    if (frame.image != null) {
                        filter.push(frame);
                        Frame out = filter.pull();
                        if (out != null && out.image != null) {
                            return converter.convert(out);
                        }
                    }
                }
                filter.push(null);
                Frame out = filter.pull();
                if (out != null && out.image != null) {
                    return converter.convert(out);
                }
            }
            grabber.stop();
        }
        return null;
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

    /**
     * 从抽帧目录加载图片（支持 extract_frames 多帧输出），用于雪碧图。
     * 匹配 frame_0.png / frame_1.jpg 等命名（多帧时为 TASK_ID/frame_INDEX.ext），按文件名排序取前 count 张。
     */
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

    /**
     * 使用 ImageMagick 命令行进行图片格式转换、缩放、质量调整。
     * 支持单文件或目录（递归处理目录内图片）。
     */
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

    /**
     * 按时长均匀抽帧（雪碧图用），使用 setTimestamp 兼容性优于 setFrameNumber。
     */
    private List<BufferedImage> extractFramesByTimestamp(String inputPath, int count) throws Exception {
        if (count <= 0) return new ArrayList<>();
        List<BufferedImage> list = new ArrayList<>();
        try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(inputPath);
             Java2DFrameConverter converter = new Java2DFrameConverter()) {
            grabber.start();
            long durationUs = grabber.getLengthInTime();
            if (durationUs <= 0) return new ArrayList<>();
            for (int i = 0; i < count; i++) {
                long ts = (count <= 1) ? 0 : (i * durationUs) / (count - 1);
                grabber.setTimestamp(ts);
                Frame frame = grabber.grab();
                if (frame != null && frame.image != null) {
                    BufferedImage img = converter.convert(frame);
                    if (img != null) list.add(img);
                }
            }
            grabber.stop();
        }
        return list;
    }

    /**
     * 使用 JavaCV 从视频中按间隔抽帧，返回 BufferedImage 列表。
     * 抽帧步骤使用此方法，顺序 grab 兼容性最好。
     */
    private List<BufferedImage> extractFramesByInterval(String inputPath, int interval, int maxFrames, java.util.function.BooleanSupplier isCancelled) throws Exception {
        List<BufferedImage> list = new ArrayList<>();
        try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(inputPath);
             Java2DFrameConverter converter = new Java2DFrameConverter()) {
            grabber.start();
            Frame frame;
            int videoFrameIndex = 0;
            while (list.size() < maxFrames && (frame = grabber.grab()) != null) {
                if (isCancelled != null && isCancelled.getAsBoolean()) {
                    throw new IOException("任务已取消");
                }
                if (frame.image != null) {
                    if (videoFrameIndex % interval == 0) {
                        BufferedImage img = converter.convert(frame);
                        if (img != null) list.add(img);
                    }
                    videoFrameIndex++;
                }
            }
            grabber.stop();
        }
        return list;
    }

    public void addWatermark(String inputFile, String outputFile, String watermarkPath, String position) throws Exception {
        if (watermarkPath == null || watermarkPath.isBlank() || !new File(watermarkPath).exists()) {
            Files.copy(new File(inputFile).toPath(), new File(outputFile).toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
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

    /**
     * 若路径以视频扩展名结尾（mp4/mpg/avi/mov 等），替换为图片扩展名，供雪碧图等图片输出使用
     */
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

    /**
     * 将模板解析出的路径规范为本地文件路径：去掉 URL 中的 ? 及后续查询串（替换为 _），
     * 相对路径则基于 workDir 转为绝对路径，避免 FFmpeg 报错 -2。
     */
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
