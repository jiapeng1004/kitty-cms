package icu.jiapeng.kitty.plugin.s3.adapter.local;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import icu.jiapeng.kitty.plugin.s3.config.S3Properties;
import icu.jiapeng.kitty.plugin.s3.domain.MultipartUpload;
import icu.jiapeng.kitty.plugin.s3.domain.S3Object;
import icu.jiapeng.kitty.plugin.s3.port.MetadataStorePort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 文件系统持久化元数据存储实现
 * 所有元数据都持久化到磁盘，重启不丢失
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "kitty.s3.metadata", name = "type", havingValue = "local", matchIfMissing = true)
public class FileSystemMetadataStoreAdapter implements MetadataStorePort {

    private final String basePath;

    public FileSystemMetadataStoreAdapter(S3Properties properties) {
        this.basePath = properties.getStorage().getLocal().getBasePath();
        File rootDir = new File(basePath);
        if (!rootDir.exists()) {
            rootDir.mkdirs();
        }
    }

    @Override
    public List<String> listBuckets() {
        try {
            return Files.list(Paths.get(basePath))
                    .filter(Files::isDirectory)
                    .map(path -> path.getFileName().toString())
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException("Failed to list buckets", e);
        }
    }

    @Override
    public void saveBucket(String bucketName, String owner) {
        Path bucketPath = Paths.get(basePath, bucketName);
        Path metaPath = Paths.get(basePath, bucketName, ".s3meta");
        Path objectsPath = Paths.get(basePath, bucketName, "objects");
        Path multipartPath = Paths.get(basePath, bucketName, "multipart");

        try {
            Files.createDirectories(bucketPath);
            Files.createDirectories(metaPath);
            Files.createDirectories(objectsPath);
            Files.createDirectories(multipartPath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create bucket: " + bucketName, e);
        }
    }

    @Override
    public boolean bucketExists(String bucketName) {
        return Files.exists(Paths.get(basePath, bucketName));
    }

    @Override
    public void deleteBucket(String bucketName) {
        Path bucketPath = Paths.get(basePath, bucketName);
        try {
            deleteDirectoryRecursively(bucketPath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete bucket: " + bucketName, e);
        }
    }

    @Override
    public void saveObjectMetadata(S3Object object) {
        Path metaPath = getObjectMetaPath(object.getBucketName(), object.getKey());
        try {
            Files.createDirectories(metaPath.getParent());
            String json = JSON.toJSONString(object, SerializerFeature.PrettyFormat, SerializerFeature.WriteDateUseDateFormat);
            Files.writeString(metaPath, json);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save object metadata: " + object.getKey(), e);
        }
    }

    @Override
    public Optional<S3Object> getObjectMetadata(String bucket, String key) {
        Path metaPath = getObjectMetaPath(bucket, key);
        if (!Files.exists(metaPath)) {
            return Optional.empty();
        }
        try {
            String json = Files.readString(metaPath);
            S3Object object = JSON.parseObject(json, S3Object.class);
            return Optional.of(object);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read object metadata: " + key, e);
        }
    }

    @Override
    public void deleteObjectMetadata(String bucket, String key) {
        Path metaPath = getObjectMetaPath(bucket, key);
        try {
            Files.deleteIfExists(metaPath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete object metadata: " + key, e);
        }
    }

    @Override
    public void saveMultipartUpload(MultipartUpload upload) {
        Path uploadPath = getMultipartUploadPath(upload.getBucketName(), upload.getUploadId());
        try {
            Files.createDirectories(uploadPath.getParent());
            String json = JSON.toJSONString(upload, SerializerFeature.PrettyFormat, SerializerFeature.WriteDateUseDateFormat);
            Files.writeString(uploadPath, json);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save multipart upload: " + upload.getUploadId(), e);
        }
    }

    @Override
    public Optional<MultipartUpload> getMultipartUpload(String uploadId) {
        // 扫描所有桶查找分块上传
        try {
            List<String> buckets = listBuckets();
            for (String bucket : buckets) {
                Path uploadPath = getMultipartUploadPath(bucket, uploadId);
                if (Files.exists(uploadPath)) {
                    String json = Files.readString(uploadPath);
                    MultipartUpload upload = JSON.parseObject(json, MultipartUpload.class);
                    return Optional.of(upload);
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("Multipart upload not found: {}", uploadId);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to get multipart upload: " + uploadId, e);
        }
        return Optional.empty();
    }

    @Override
    public void deleteMultipartUpload(String uploadId) {
        Optional<MultipartUpload> uploadOpt = getMultipartUpload(uploadId);
        if (uploadOpt.isPresent()) {
            MultipartUpload upload = uploadOpt.get();
            Path uploadPath = getMultipartUploadPath(upload.getBucketName(), uploadId);
            try {
                Files.deleteIfExists(uploadPath);
            } catch (IOException e) {
                throw new RuntimeException("Failed to delete multipart upload: " + uploadId, e);
            }
        }
    }

    private Path getObjectMetaPath(String bucket, String key) {
        return Paths.get(basePath, bucket, ".s3meta", "objects", key + ".json");
    }

    private Path getMultipartUploadPath(String bucket, String uploadId) {
        return Paths.get(basePath, bucket, ".s3meta", "multipart", uploadId + ".json");
    }

    private void deleteDirectoryRecursively(Path path) throws IOException {
        if (Files.isDirectory(path)) {
            Files.list(path).forEach(child -> {
                try {
                    deleteDirectoryRecursively(child);
                } catch (IOException e) {
                    throw new RuntimeException("Failed to delete directory: " + child, e);
                }
            });
        }
        Files.delete(path);
    }
}
