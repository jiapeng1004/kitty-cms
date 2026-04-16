package icu.jiapeng.kitty.material.storage;

import org.springframework.util.StringUtils;

import java.net.URLConnection;
import java.util.Locale;
import java.util.Map;

/**
 * 对象存储写入时的 Content-Type 推断（便于浏览器 / MinIO 控制台按类型预览）。
 */
public final class StorageMimeTypes {

    private static final String DEFAULT = "application/octet-stream";

    private static final Map<String, String> BY_EXT = Map.ofEntries(
            Map.entry("png", "image/png"),
            Map.entry("jpg", "image/jpeg"),
            Map.entry("jpeg", "image/jpeg"),
            Map.entry("gif", "image/gif"),
            Map.entry("webp", "image/webp"),
            Map.entry("bmp", "image/bmp"),
            Map.entry("svg", "image/svg+xml"),
            Map.entry("mp4", "video/mp4"),
            Map.entry("webm", "video/webm"),
            Map.entry("mov", "video/quicktime"),
            Map.entry("mkv", "video/x-matroska"),
            Map.entry("mp3", "audio/mpeg"),
            Map.entry("wav", "audio/wav"),
            Map.entry("flac", "audio/flac"),
            Map.entry("aac", "audio/aac"),
            Map.entry("pdf", "application/pdf"),
            Map.entry("txt", "text/plain"),
            Map.entry("html", "text/html"),
            Map.entry("htm", "text/html"),
            Map.entry("css", "text/css"),
            Map.entry("js", "text/javascript"),
            Map.entry("json", "application/json"),
            Map.entry("xml", "application/xml"),
            Map.entry("zip", "application/zip"),
            Map.entry("doc", "application/msword"),
            Map.entry("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"),
            Map.entry("xls", "application/vnd.ms-excel"),
            Map.entry("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
            Map.entry("ppt", "application/vnd.ms-powerpoint"),
            Map.entry("pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation")
    );

    private StorageMimeTypes() {
    }

    /**
     * 优先用资源标题（通常含扩展名），否则用对象键最后一段路径推断。
     */
    public static String resolveFromTitleAndObjectKey(String title, String objectKey) {
        String fromTitle = guessFromFilename(title);
        if (!DEFAULT.equals(fromTitle)) {
            return fromTitle;
        }
        if (!StringUtils.hasText(objectKey)) {
            return DEFAULT;
        }
        int slash = objectKey.lastIndexOf('/');
        String base = slash >= 0 ? objectKey.substring(slash + 1) : objectKey;
        return guessFromFilename(base);
    }

    private static String guessFromFilename(String name) {
        if (!StringUtils.hasText(name)) {
            return DEFAULT;
        }
        String trimmed = name.trim();
        String jdk = URLConnection.guessContentTypeFromName(trimmed);
        if (StringUtils.hasText(jdk) && !DEFAULT.equals(jdk)) {
            return jdk;
        }
        int dot = trimmed.lastIndexOf('.');
        if (dot < 0 || dot >= trimmed.length() - 1) {
            return DEFAULT;
        }
        String ext = trimmed.substring(dot + 1).toLowerCase(Locale.ROOT);
        return BY_EXT.getOrDefault(ext, DEFAULT);
    }
}
