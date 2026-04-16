package icu.jiapeng.kitty.material.upload.entity.chunk;

import icu.jiapeng.kitty.common.core.constant.ResultStatus;
import icu.jiapeng.kitty.common.core.exceptions.BizException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Comparator;
import java.util.stream.Stream;

/**
 * 本地目录暂存分片：{@code {stagingRoot}/{sessionId}/{index}.part}。
 */
@Component
public class FilesystemChunkStagingService {

    private final Path stagingRoot;

    public FilesystemChunkStagingService(
            @Value("${material.chunk.staging-dir:}") String configuredStagingDir) {
        if (configuredStagingDir != null && !configuredStagingDir.isBlank()) {
            this.stagingRoot = Path.of(configuredStagingDir.trim());
        } else {
            this.stagingRoot = Path.of(System.getProperty("java.io.tmpdir"), "kitty-material-chunk");
        }
    }

    private Path sessionDir(String sessionId) {
        return stagingRoot.resolve(sanitizeSegment(sessionId));
    }

    private Path partPath(String sessionId, int chunkIndex) {
        return sessionDir(sessionId).resolve(chunkIndex + ".part");
    }

    private static String sanitizeSegment(String sessionId) {
        if (sessionId == null || sessionId.isBlank() || sessionId.contains("..") || sessionId.contains("/") || sessionId.contains("\\")) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        return sessionId;
    }

    public String writePart(String sessionId, int chunkIndex, byte[] data) {
        if (data == null) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        try {
            Path dir = sessionDir(sessionId);
            Files.createDirectories(dir);
            Path tmp = partPath(sessionId, chunkIndex).resolveSibling(chunkIndex + ".part.tmp");
            Path target = partPath(sessionId, chunkIndex);
            Files.write(tmp, data, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
            Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return null;
    }

    public boolean partExists(String sessionId, int chunkIndex) {
        return Files.isRegularFile(partPath(sessionId, chunkIndex));
    }

    public long partByteSize(String sessionId, int chunkIndex) {
        Path p = partPath(sessionId, chunkIndex);
        try {
            if (!Files.isRegularFile(p)) {
                throw BizException.of(ResultStatus.PARAM_ERROR);
            }
            return Files.size(p);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public Path resolvePartPath(String sessionId, int chunkIndex) {
        return partPath(sessionId, chunkIndex);
    }

    public void deleteSession(String sessionId) {
        try {
            Path dir = sessionDir(sessionId);
            if (!Files.isDirectory(dir)) {
                return;
            }
            try (Stream<Path> walk = Files.walk(dir)) {
                walk.sorted(Comparator.reverseOrder()).forEach(p -> {
                    try {
                        Files.deleteIfExists(p);
                    } catch (IOException ignored) {
                    }
                });
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
