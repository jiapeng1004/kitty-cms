package icu.jiapeng.kitty.material.upload;

/**
 * 分片上传会话状态。
 */
public final class ChunkUploadSessionStatus {

    private ChunkUploadSessionStatus() {
    }

    public static final String UPLOADING = "uploading";

    public static final String COMPLETED = "completed";

    public static final String CANCELLED = "cancelled";
}
