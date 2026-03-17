package icu.jiapeng.kitty.transcoder.func.controller;

import icu.jiapeng.kitty.transcoder.func.preview.PreviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/transcode")
@Tag(name = "预览", description = "转码输出文件流式预览、可预览文件列表")
public class PreviewController {

    @Resource
    private PreviewService previewService;

    private static final Map<String, String> MIME_TYPES = Map.ofEntries(
            Map.entry("mp4", "video/mp4"),
            Map.entry("m4v", "video/x-m4v"),
            Map.entry("webm", "video/webm"),
            Map.entry("mkv", "video/x-matroska"),
            Map.entry("avi", "video/x-msvideo"),
            Map.entry("mov", "video/quicktime"),
            Map.entry("jpg", "image/jpeg"),
            Map.entry("jpeg", "image/jpeg"),
            Map.entry("png", "image/png"),
            Map.entry("gif", "image/gif"),
            Map.entry("webp", "image/webp")
    );

    @Operation(summary = "流式预览文件", description = "path 为空则返回主输出文件")
    @GetMapping("/preview")
    public ResponseEntity<org.springframework.core.io.Resource> preview(
            @RequestParam @Parameter(description = "任务 ID") String taskId,
            @RequestParam(required = false) @Parameter(description = "相对路径，空则主输出") String path) {
        File file = previewService.resolveFile(taskId, path);
        if (file == null) {
            return ResponseEntity.notFound().build();
        }
        org.springframework.core.io.Resource resource = new FileSystemResource(file);
        String contentType = guessContentType(file.getName(), path);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getName() + "\"")
                .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                .body(resource);
    }

    @Operation(summary = "可预览文件列表", description = "返回路径与预览链接，baseUrl 可选用于生成完整 URL")
    @GetMapping("/preview/info")
    public Map<String, Object> previewInfo(
            @RequestParam String taskId,
            @RequestParam(required = false) String baseUrl) {
        List<PreviewService.PreviewItem> items = previewService.listPreviewFiles(taskId);
        String prefix = (baseUrl != null && !baseUrl.isBlank()) ? baseUrl.replaceAll("/$", "") : "";
        List<Map<String, String>> files = items.stream()
                .map(i -> {
                    String previewUrl = prefix + "/api/transcode/preview?taskId=" + taskId
                            + (i.getPath().isEmpty() ? "" : "&path=" + URLEncoder.encode(i.getPath(), StandardCharsets.UTF_8));
                    return Map.<String, String>of(
                            "path", i.getAbsolutePath(),
                            "previewUrl", previewUrl
                    );
                })
                .collect(Collectors.toList());
        return Map.of(
                "taskId", taskId,
                "files", files
        );
    }

    private static String guessContentType(String fileName, String pathHint) {
        String mime = guessFromName(fileName);
        if (mime != null) return mime;
        if (pathHint != null && !pathHint.isBlank()) {
            mime = guessFromName(pathHint);
            if (mime != null) return mime;
        }
        return "application/octet-stream";
    }

    private static String guessFromName(String name) {
        int dot = name.lastIndexOf('.');
        if (dot >= 0 && dot < name.length() - 1) {
            String ext = name.substring(dot + 1).toLowerCase().replaceAll("[^a-z0-9]", "");
            if (!ext.isEmpty()) {
                String mime = MIME_TYPES.get(ext);
                if (mime != null) return mime;
            }
        }
        return null;
    }
}
