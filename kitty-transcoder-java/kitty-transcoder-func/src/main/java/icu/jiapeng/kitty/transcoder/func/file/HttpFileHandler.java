package icu.jiapeng.kitty.transcoder.func.file;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * HTTP 输入处理：将 URL 下载到本地临时目录，返回本地路径。
 */
public final class HttpFileHandler {

    private HttpFileHandler() {}

    /**
     * 将 HTTP URL 下载到临时目录，返回本地文件路径。
     * @param url HTTP URL
     * @param taskId 任务 ID，用于子目录命名
     * @param tempDir 临时根目录
     * @return 本地文件路径
     */
    public static String downloadToTemp(String url, String taskId, String tempDir) {
        return downloadToTemp(url, taskId, tempDir, "input_" + System.currentTimeMillis() + ".mp4");
    }

    /** 下载到临时目录，指定无扩展名时的默认文件名 */
    public static String downloadToTemp(String url, String taskId, String tempDir, String defaultFileName) {
        try {
            Path base = Paths.get(tempDir, taskId);
            Files.createDirectories(base);
            String fileName = url.substring(url.lastIndexOf('/') + 1);
            if (fileName.isEmpty() || !fileName.contains(".")) {
                fileName = defaultFileName;
            }
            Path target = base.resolve(fileName);
            try (InputStream in = URI.create(url).toURL().openStream()) {
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            }
            return target.toAbsolutePath().toString();
        } catch (IOException e) {
            throw new RuntimeException("下载 HTTP 文件失败: " + url, e);
        }
    }
}
