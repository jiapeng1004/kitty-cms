package icu.jiapeng.kitty.material.upload.chunk;

import java.nio.file.Path;

/**
 * 分片暂存（供合并前落盘）：二进制分片上传写入，完成/取消时清理。
 */
public interface ChunkStagingPort {

    void writePart(String sessionId, int chunkIndex, byte[] data);

    boolean partExists(String sessionId, int chunkIndex);

    long partByteSize(String sessionId, int chunkIndex);

    Path resolvePartPath(String sessionId, int chunkIndex);

    void deleteSession(String sessionId);
}
