package icu.jiapeng.kitty.transcoder.func.engine;

import icu.jiapeng.kitty.transcoder.api.StrategyStepVO;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@Component
public class MediaStepOps {

    public String doTranscode(String inputPath, StrategyStepVO step, String stepSuffix, String resolvedOutputPath) throws Exception {
        File input = new File(inputPath);
        if (input.isDirectory()) throw new IOException("转码步骤需要文件输入，当前为目录");
        int width = 1920, height = 1080;
        if (step.getResolution() != null && !step.getResolution().isBlank()) {
            String[] parts = step.getResolution().split("[xX×]");
            if (parts.length >= 2) {
                try {
                    width = Integer.parseInt(parts[0].trim());
                    height = Integer.parseInt(parts[1].trim());
                } catch (NumberFormatException ignored) { }
            }
        }
        String format = step.getTargetFormat() != null ? step.getTargetFormat() : "mp4";
        String outputFile = resolvedOutputPath != null && !resolvedOutputPath.isBlank()
                ? resolvedOutputPath
                : parentPath(inputPath) + File.separator + baseName(inputPath) + stepSuffix + "." + format;
        int bitrate = (step.getBitrate() != null ? step.getBitrate() : 5000) * 1000;
        double frameRate = step.getFrameRate() != null ? step.getFrameRate() : 30;
        String codec = step.getEncoder() != null ? step.getEncoder() : "h264";

        try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(inputPath);
             FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputFile, width, height, 2)) {
            grabber.start();
            recorder.setFormat(format);
            recorder.setVideoCodecName(codec);
            recorder.setVideoBitrate(bitrate);
            recorder.setFrameRate(frameRate);
            recorder.setAudioCodecName("aac");
            recorder.setAudioBitrate(128000);
            recorder.setSampleRate(44100);
            recorder.start();
            Frame frame;
            while ((frame = grabber.grab()) != null) recorder.record(frame);
            recorder.stop();
            grabber.stop();
        }
        // 转码步骤若配置了水印则顺带加水印，否则仅转码
        if (step.getWatermarkPath() != null && !step.getWatermarkPath().isBlank()) {
            File wmFile = new File(step.getWatermarkPath());
            if (wmFile.exists()) {
                String watermarked = outputFile + ".wmtmp";
                addWatermark(outputFile, watermarked, step.getWatermarkPath(), step.getWatermarkPosition());
                Files.delete(Paths.get(outputFile));
                Files.move(Paths.get(watermarked), Paths.get(outputFile), StandardCopyOption.REPLACE_EXISTING);
            }
        }
        return outputFile;
    }

    public String doExtractFrames(String inputPath, StrategyStepVO step, String stepSuffix, String resolvedOutputPath) throws Exception {
        String outDir = resolvedOutputPath != null && !resolvedOutputPath.isBlank()
                ? resolvedOutputPath
                : parentPath(inputPath) + File.separator + baseName(inputPath) + stepSuffix + "_frames";
        File dir = new File(outDir);
        if (!dir.exists()) dir.mkdirs();
        int interval = step.getFrameInterval() != null && step.getFrameInterval() > 0 ? step.getFrameInterval() : 30;
        int frameCount = step.getExtractFrameCount() != null && step.getExtractFrameCount() > 0 ? step.getExtractFrameCount() : 1;
        String fmt = "jpg";
        if (step.getExtractOutputFormat() != null && step.getExtractOutputFormat().equalsIgnoreCase("png")) fmt = "png";
        String pattern = outDir + File.separator + "frame_%04d." + fmt;
        ProcessBuilder pb = new ProcessBuilder("ffmpeg", "-y", "-i", inputPath,
                "-vf", "select=not(mod(n\\," + interval + "))", "-vsync", "vfr", "-frames:v", String.valueOf(frameCount), pattern);
        pb.inheritIO();
        Process p = pb.start();
        if (p.waitFor() != 0) throw new IOException("ffmpeg 抽帧失败");
        return outDir;
    }

    public String doSpriteSheet(String inputPath, StrategyStepVO step, String stepSuffix, String resolvedOutputPath) throws Exception {
        File input = new File(inputPath);
        if (input.isDirectory()) throw new IOException("雪碧图步骤需要文件输入，当前为目录");
        int cols = step.getSpriteColumns() != null && step.getSpriteColumns() > 0 ? step.getSpriteColumns() : 4;
        int rows = step.getSpriteRows() != null && step.getSpriteRows() > 0 ? step.getSpriteRows() : 3;
        int interval = step.getFrameInterval() != null && step.getFrameInterval() > 0 ? step.getFrameInterval() : 30;
        String fmt = "jpg";
        if (step.getExtractOutputFormat() != null && step.getExtractOutputFormat().equalsIgnoreCase("png")) fmt = "png";
        String baseName = baseName(inputPath);
        File tempDir = new File(parentPath(inputPath), baseName + stepSuffix + "_sprite_tmp");
        if (!tempDir.exists()) tempDir.mkdirs();
        try {
            String pattern = tempDir.getAbsolutePath() + File.separator + "frame_%04d." + fmt;
            ProcessBuilder pb = new ProcessBuilder("ffmpeg", "-y", "-i", inputPath,
                    "-vf", "select=not(mod(n\\," + interval + "))", "-vsync", "vfr", "-frames:v", String.valueOf(cols * rows), pattern);
            pb.inheritIO();
            Process p = pb.start();
            if (p.waitFor() != 0) throw new IOException("ffmpeg 抽帧失败");
            File[] files = tempDir.listFiles((d, n) -> n != null && n.startsWith("frame_") && (n.endsWith(".jpg") || n.endsWith(".png")));
            if (files == null || files.length == 0) throw new IOException("未抽到帧");
            Arrays.sort(files, Comparator.comparing(File::getName));
            List<BufferedImage> images = new ArrayList<>();
            int maxW = 0, maxH = 0;
            for (int i = 0; i < Math.min(files.length, cols * rows); i++) {
                BufferedImage img = ImageIO.read(files[i]);
                if (img != null) {
                    images.add(img);
                    maxW = Math.max(maxW, img.getWidth());
                    maxH = Math.max(maxH, img.getHeight());
                }
            }
            if (images.isEmpty()) throw new IOException("无法读取抽帧图片");
            BufferedImage sprite = new BufferedImage(maxW * cols, maxH * rows, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = sprite.createGraphics();
            for (int i = 0; i < images.size(); i++) {
                int r = i / cols, c = i % cols;
                g.drawImage(images.get(i), c * maxW, r * maxH, maxW, maxH, null);
            }
            g.dispose();
            String outPath = resolvedOutputPath != null && !resolvedOutputPath.isBlank()
                    ? resolvedOutputPath
                    : parentPath(inputPath) + File.separator + baseName + stepSuffix + "_sprite." + fmt;
            ImageIO.write(sprite, fmt, new File(outPath));
            return outPath;
        } finally {
            File[] list = tempDir.listFiles();
            if (list != null) for (File f : list) if (f != null) f.delete();
            tempDir.delete();
        }
    }

    public void addWatermark(String inputFile, String outputFile, String watermarkPath, String position) throws Exception {
        if (watermarkPath == null || watermarkPath.isBlank() || !new File(watermarkPath).exists()) {
            Files.copy(new File(inputFile).toPath(), new File(outputFile).toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            return;
        }
        String overlay = "W-w-10:H-h-10";
        if (position != null) {
            switch (position.toLowerCase()) {
                case "top-left": overlay = "10:10"; break;
                case "top-right": overlay = "W-w-10:10"; break;
                case "bottom-left": overlay = "10:H-h-10"; break;
                default: overlay = "W-w-10:H-h-10";
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
}
