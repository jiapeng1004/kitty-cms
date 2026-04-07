package icu.jiapeng.kitty.plugin.s3.port;

import icu.jiapeng.kitty.plugin.s3.model.dto.ListObjectsRequest;
import icu.jiapeng.kitty.plugin.s3.model.dto.MultipartComposeResult;
import icu.jiapeng.kitty.plugin.s3.model.dto.PutObjectResult;
import lombok.*;

import java.io.InputStream;
import java.util.List;

/**
 * 存储后端端口抽象
 * 适配不同存储系统实现
 */
public interface StorageBackendPort {

    /**
     * 存储对象
     *
     * @param bucket  桶名
     * @param key     对象键
     * @param content 对象内容流
     * @param size    对象大小
     * @return ETag哈希值
     */
    PutObjectResult putObject(String bucket, String key, InputStream content, long size);

    /**
     * 获取对象
     *
     * @param bucket 桶名
     * @param key    对象键
     * @return 对象内容流
     */
    InputStream getObject(String bucket, String key);

    /**
     * 删除对象
     *
     * @param bucket 桶名
     * @param key    对象键
     */
    void deleteObject(String bucket, String key);

    /**
     * 检查对象是否存在
     *
     * @param bucket 桶名
     * @param key    对象键
     * @return 是否存在
     */
    boolean exists(String bucket, String key);

    /**
     * 列出对象
     *
     * @return 对象列表
     */
    List<ObjectInfo> listObjects(ListObjectsRequest request);

    /**
     * 将已存在的分片对象按顺序合并为最终对象。
     * 本地实现可流式拼接；远程 S3 等实现可改为服务端 CompleteMultipartUpload / ComposeObject 等，不在应用层读流合并。
     *
     * @param bucket          桶名
     * @param destinationKey  最终对象键
     * @param partKeysInOrder 分片对象键，顺序即拼接顺序
     * @return 合并后对象的 ETag 与真实大小
     */
    MultipartComposeResult composeMultipartParts(String bucket, String destinationKey, List<String> partKeysInOrder);

    /**
     * 列出某次分片上传会话已落盘的分片对象（逻辑 key 形如 {@code .s3multipart/{uploadId}/{partNumber}}）。
     * 用于完成/中止时以磁盘为准，避免在元数据里逐片追加 JSON。
     */
    List<ObjectInfo> listMultipartParts(String bucket, String uploadId);

    /**
     * 对象信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class ObjectInfo {
        private String key;
        private long size;
        private String eTag;
        private long lastModified;
    }
}
