package icu.jiapeng.kitty.material.upload.chunk;

import java.nio.file.Path;

/**
 * 分片写入与清理：磁盘引擎写入本地 staging；对象存储引擎直传 UploadPart。
 */
public interface ChunkStagingPort {

    /**
     * @return 对象存储分片的 ETag；纯本地暂存返回 null
     */
    String writePart(String sessionId, int chunkIndex, byte[] data);

    boolean partExists(String sessionId, int chunkIndex);

    long partByteSize(String sessionId, int chunkIndex);

    Path resolvePartPath(String sessionId, int chunkIndex);

    void deleteSession(String sessionId);
}
