package icu.jiapeng.kitty.material.upload.service;

import icu.jiapeng.kitty.material.upload.ChunkUploadSessionCreateSpec;
import icu.jiapeng.kitty.material.upload.entity.KtChunkUploadSession;

public interface ChunkUploadSessionService {

    static int computeChunkCount(long totalSize, long chunkSize) {
        if (totalSize == 0) {
            return 0;
        }
        return (int) ((totalSize + chunkSize - 1) / chunkSize);
    }

    KtChunkUploadSession create(ChunkUploadSessionCreateSpec spec);

    void registerPart(String sessionId, int chunkIndex, long byteSize);

    int resolveChunkIndexForByteRange(KtChunkUploadSession session, long firstByteInclusive, long lastByteInclusive);

    KtChunkUploadSession complete(String sessionId);

    KtChunkUploadSession cancel(String sessionId);

    long expectedChunkByteSize(KtChunkUploadSession session, int chunkIndex);
}
