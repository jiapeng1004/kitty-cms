package icu.jiapeng.kitty.plugin.s3.domain;

import lombok.*;

import java.util.Comparator;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

/**
 * 分块上传领域模型。
 *
 * <p>分片索引使用字符串键（如 {@code "1"}）持久化，避免 JSON 往返后整型键解析不一致。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MultipartUpload {
    private String uploadId;
    private String bucketName;
    private String key;
    private String contentType;
    private Date createTime;
    /** key 为 {@link String#valueOf(int)} 的分片序号 */
    @Builder.Default
    private Map<String, Part> parts = new TreeMap<>(Comparator.comparingInt(Integer::parseInt));

    public MultipartUpload(String uploadId, String bucketName, String key, String contentType) {
        this.uploadId = uploadId;
        this.bucketName = bucketName;
        this.key = key;
        this.contentType = contentType;
        this.createTime = new Date();
        this.parts = new TreeMap<>(Comparator.comparingInt(Integer::parseInt));
    }

    /**
     * 添加上传分块
     */
    public void addPart(int partNumber, String eTag, long size) {
        if (parts == null) {
            parts = new TreeMap<>(Comparator.comparingInt(Integer::parseInt));
        }
        parts.put(String.valueOf(partNumber), new Part(partNumber, eTag, size));
    }

    public Part getPart(int partNumber) {
        return parts == null ? null : parts.get(String.valueOf(partNumber));
    }

    /**
     * 分块信息
     */
    @Data
    @Builder
    @AllArgsConstructor
    public static class Part {
        private int partNumber;
        private String eTag;
        private long size;
    }
}
