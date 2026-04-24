package icu.jiapeng.kitty.material.upload;

import lombok.Data;

import java.util.List;

/**
 * 创建分片上传会话的输入：{@code existingResourceId} 非空为旧版「已有资源」；否则为 MAM 建档后上传。
 */
@Data
public class ChunkUploadSessionCreateSpec {

    private String existingResourceId;

    private String catalogId;
    private String parentResourceId;
    private String title;
    private Integer resourceType;

    private List<Long> chunkCrc32Unsigned;

    private String storageId;
    private String objectKey;
    private long totalSize;
    private long chunkSize;

    private String precatalogJson;

    private String transcodeStrategyId;
}
