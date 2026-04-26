package icu.jiapeng.kitty.material.storage;

import icu.jiapeng.kitty.material.resource.entity.KtFileStorage;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * 存储驱动端口。
 */
public interface StorageDriver {

    String driverName();

    StorageRouteResult route(StorageRouteRequest request, KtFileStorage storageConfig);

    /**
     * 写入空对象（分片会话 total_size=0 的完成路径）。
     *
     * @param contentTypeOrNull 非空时写入对象元数据 Content-Type（S3 预览依赖）；null 时由驱动决定默认
     */
    void putEmptyObject(KtFileStorage storageConfig, String objectKey, String contentTypeOrNull) throws IOException;

    /**
     * 将本地已落盘的分片文件按顺序拼接为最终对象（仅磁盘引擎：分片暂存在本地 staging）。
     */
    void writeSequentialLocalPartFilesToObject(KtFileStorage storageConfig, String objectKey, List<Path> orderedLocalPartPaths) throws IOException;

    /**
     * 对象存储：创建分片上传，返回 uploadId。
     *
     * @param contentTypeOrNull 非空时作为最终对象的 Content-Type（在 CreateMultipartUpload 上设置，便于预览）
     */
    default String initiateMultipartUpload(KtFileStorage storageConfig, String objectKey, String contentTypeOrNull) throws IOException {
        throw new IOException("multipart upload not supported: " + driverName());
    }

    /**
     * 对象存储：上传一片，返回 ETag（不含引号）。
     */
    default String uploadMultipartPart(KtFileStorage storageConfig, String objectKey, String uploadId, int partNumber, byte[] data) throws IOException {
        throw new IOException("multipart upload not supported: " + driverName());
    }

    default void completeMultipartUpload(KtFileStorage storageConfig, String objectKey, String uploadId, List<StoragePartEtag> parts) throws IOException {
        throw new IOException("multipart upload not supported: " + driverName());
    }

    default void abortMultipartUpload(KtFileStorage storageConfig, String objectKey, String uploadId) throws IOException {
        throw new IOException("multipart upload not supported: " + driverName());
    }

    default String normalizeObjectKey(String objectKey) {
        return objectKey == null ? "" : objectKey.trim();
    }

    /**
     * 删除已存储对象；若不存在则各实现可忽略或视为成功。
     */
    default void deleteObject(KtFileStorage storageConfig, String objectKey) throws IOException {
        throw new IOException("delete not supported: " + driverName());
    }

    default boolean isValidObjectKey(String objectKey) {
        return objectKey != null && !objectKey.isBlank();
    }

    default StorageConnectivityResult testConnectivity(String storageId, KtFileStorage storageConfig) {
        StorageConnectivityResult result = new StorageConnectivityResult();
        result.setStorageId(storageId);
        result.setDriverName(driverName());
        result.setReachable(true);
        result.setDetail("ok");
        return result;
    }
}
