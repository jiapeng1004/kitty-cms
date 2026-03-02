package icu.jiapeng.kitty.transcoder.func.preview;

import icu.jiapeng.kitty.transcoder.func.entity.TranscodeTask;
import icu.jiapeng.kitty.transcoder.func.mapper.TranscodeTaskMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/**
 * 预览服务：根据任务 ID 和相对路径解析并列出可预览文件，不依赖 output.httpPrefix 配置。
 */
@Service
public class PreviewService {

    @Resource
    private TranscodeTaskMapper taskMapper;

    /**
     * 解析预览文件，path 为空时返回主输出文件；path 非空时相对于 outputPath 所在目录解析
     *
     * @param taskId 任务 ID
     * @param path   相对路径（可为空，表示主输出；非空则为 output 所在目录下的相对路径）
     * @return 文件，不存在或越界则 null
     */
    public File resolveFile(String taskId, String path) {
        TranscodeTask task = taskMapper.selectById(taskId);
        if (task == null || task.getOutputPath() == null || task.getOutputPath().isBlank()) return null;

        File outputFile = new File(task.getOutputPath());
        if (!outputFile.exists()) return null;

        if (path == null || path.isBlank()) {
            return outputFile.isFile() ? outputFile : null;
        }

        Path baseDir = outputFile.isDirectory() ? outputFile.toPath() : outputFile.getParentFile().toPath();
        try {
            Path resolved = baseDir.resolve(path.trim()).normalize();
            Path baseAbs = baseDir.toAbsolutePath().normalize();
            if (!resolved.startsWith(baseAbs)) {
                Path parent = baseAbs.getParent();
                if (parent == null || !resolved.startsWith(parent)) return null;
            }
            File f = resolved.toFile();
            return f.exists() && f.isFile() ? f : null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 列出任务可预览文件：主输出 + 同目录、父目录及子目录下的相关文件（转码、抽帧、雪碧图等）
     *
     * @param taskId 任务 ID
     * @return 路径与预览链接列表
     */
    public List<PreviewItem> listPreviewFiles(String taskId) {
        Set<String> seenAbs = new LinkedHashSet<>();
        List<PreviewItem> items = new ArrayList<>();
        TranscodeTask task = taskMapper.selectById(taskId);
        if (task == null || task.getOutputPath() == null || task.getOutputPath().isBlank()) return items;

        File output = new File(task.getOutputPath());
        if (!output.exists()) return items;

        Path baseDir = output.isDirectory() ? output.toPath() : output.getParentFile().toPath();
        Path baseDirAbs = baseDir.toAbsolutePath().normalize();
        String mainAbs = output.getAbsolutePath();

        if (output.isFile()) {
            addItem(items, seenAbs, mainAbs, "");
        }

        collectFilesFromDir(items, seenAbs, taskId, baseDir, baseDirAbs, mainAbs, "");

        Path parentDir = baseDir.getParent();
        if (parentDir != null && Files.isDirectory(parentDir)) {
            Path parentAbs = parentDir.toAbsolutePath().normalize();
            collectFilesFromDir(items, seenAbs, taskId, parentDir, parentAbs, mainAbs, "../");
        }

        return items;
    }

    private void collectFilesFromDir(List<PreviewItem> items, Set<String> seenAbs, String taskId,
                                     Path baseDir, Path baseDirAbs, String mainAbs, String relPrefix) {
        try (Stream<Path> walk = Files.walk(baseDir, 4)) {
            walk.filter(Files::isRegularFile)
                    .map(Path::toAbsolutePath)
                    .map(Path::normalize)
                    .filter(p -> p.startsWith(baseDirAbs))
                    .forEach(p -> {
                        String abs = p.toString();
                        if (abs.equals(mainAbs)) return;
                        if (!belongsToTask(abs, taskId)) return;
                        String rel = relPrefix + baseDir.relativize(p).toString().replace('\\', '/');
                        addItem(items, seenAbs, abs, rel);
                    });
        } catch (IOException ignored) {
        }
    }

    /** 仅包含属于当前任务的文件（路径含 taskId） */
    private static boolean belongsToTask(String absPath, String taskId) {
        if (taskId == null || taskId.isBlank()) return true;
        String normalized = absPath.replace('\\', '/').toLowerCase();
        return normalized.contains(taskId.toLowerCase());
    }

    private void addItem(List<PreviewItem> items, Set<String> seenAbs, String abs, String rel) {
        String key = canonicalKey(abs);
        if (key != null && seenAbs.add(key)) {
            items.add(new PreviewItem(abs, rel));
        }
    }

    private static String canonicalKey(String path) {
        try {
            return Path.of(path).toRealPath().normalize().toString().toLowerCase().replace('\\', '/');
        } catch (Exception e) {
            return path.toLowerCase().replace('\\', '/');
        }
    }

    public static class PreviewItem {
        private final String absolutePath;
        private final String path;

        public PreviewItem(String absolutePath, String path) {
            this.absolutePath = absolutePath;
            this.path = path;
        }

        public String getAbsolutePath() {
            return absolutePath;
        }

        /** 相对路径，用于构建 preview?taskId=xxx&path=xxx */
        public String getPath() {
            return path;
        }
    }
}
