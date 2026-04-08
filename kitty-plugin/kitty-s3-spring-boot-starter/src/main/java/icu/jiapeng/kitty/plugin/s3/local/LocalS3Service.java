package icu.jiapeng.kitty.plugin.s3.local;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import icu.jiapeng.kitty.plugin.s3.config.S3Properties;
import icu.jiapeng.kitty.plugin.s3.domain.MultipartUpload;
import icu.jiapeng.kitty.plugin.s3.domain.S3Object;
import icu.jiapeng.kitty.plugin.s3.model.dto.*;
import icu.jiapeng.kitty.plugin.s3.service.S3Service;
import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 本地文件系统上的 S3 后端：桶/元数据 JSON + objects 目录存内容，供开发与测试。
 */
@Component
@ConditionalOnProperty(prefix = "kitty.s3.storage", name = "type", havingValue = "local", matchIfMissing = true)
public class LocalS3Service implements S3Service {

    private static final String MULTIPART_TEMP_PREFIX = ".s3multipart/";

    private final String basePath;
    private final ObjectMapper objectMapper;

    public LocalS3Service(S3Properties properties, ObjectMapper objectMapper) {
        this.basePath = properties.getStorage().getLocal().getBasePath();
        this.objectMapper = objectMapper.copy().enable(SerializationFeature.INDENT_OUTPUT);
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
            objectMapper.writeValue(metaPath.toFile(), object);
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
            return Optional.of(objectMapper.readValue(metaPath.toFile(), S3Object.class));
        } catch (IOException e) {
            throw new RuntimeException("Failed to read object metadata: " + key, e);
        }
    }

    @Override
    public void deleteObjectMetadata(String bucket, String key) {
        try {
            Files.deleteIfExists(getObjectMetaPath(bucket, key));
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete object metadata: " + key, e);
        }
    }

    @Override
    public void saveMultipartUpload(MultipartUpload upload) {
        Path uploadPath = getMultipartUploadPath(upload.getBucketName(), upload.getUploadId());
        try {
            Files.createDirectories(uploadPath.getParent());
            objectMapper.writeValue(uploadPath.toFile(), upload);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save multipart upload: " + upload.getUploadId(), e);
        }
    }

    @Override
    public Optional<MultipartUpload> getMultipartUpload(String uploadId) {
        try {
            for (String bucket : listBuckets()) {
                Path uploadPath = getMultipartUploadPath(bucket, uploadId);
                if (Files.exists(uploadPath)) {
                    return Optional.of(objectMapper.readValue(uploadPath.toFile(), MultipartUpload.class));
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
            try {
                Files.deleteIfExists(getMultipartUploadPath(upload.getBucketName(), uploadId));
            } catch (IOException e) {
                throw new RuntimeException("Failed to delete multipart upload: " + uploadId, e);
            }
        }
    }

    @Override
    public PutObjectResult putObject(String bucket, String key, InputStream content, long size) {
        try {
            Path filePath = getObjectFilePath(bucket, key);
            File parentDir = filePath.getParent().toFile();
            if (!parentDir.exists()) {
                parentDir.mkdirs();
            }
            try (OutputStream outputStream = Files.newOutputStream(filePath)) {
                byte[] buffer = new byte[8192];
                int read;
                while ((read = content.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, read);
                }
            }
            try (InputStream inputStream = Files.newInputStream(filePath)) {
                String eTag = DigestUtils.md5DigestAsHex(inputStream);
                long actualSize = Files.size(filePath);
                return PutObjectResult.builder().eTag(eTag).size(actualSize).build();
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to store object", e);
        }
    }

    @Override
    public InputStream getObject(String bucket, String key) {
        try {
            Path filePath = getObjectFilePath(bucket, key);
            if (!Files.exists(filePath)) {
                throw new FileNotFoundException("Object not found: " + key);
            }
            return Files.newInputStream(filePath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to get object", e);
        }
    }

    @Override
    public void deleteObject(String bucket, String key) {
        try {
            Files.deleteIfExists(getObjectFilePath(bucket, key));
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete object", e);
        }
    }

    @Override
    public boolean exists(String bucket, String key) {
        return Files.exists(getObjectFilePath(bucket, key));
    }

    @Override
    public List<S3ObjectInfo> listObjects(ListObjectsRequest request) {
        List<S3ObjectInfo> result = new ArrayList<>();
        Path objectsPath = getObjectsRootPath(request.getBucketName());
        if (!Files.exists(objectsPath)) {
            return result;
        }
        try {
            Files.walk(objectsPath)
                    .filter(Files::isRegularFile)
                    .map(path -> {
                        String relativeKey = objectsPath.relativize(path).toString().replace('\\', '/');
                        if (relativeKey.startsWith(MULTIPART_TEMP_PREFIX)) {
                            return null;
                        }
                        if (request.getPrefix() != null && !relativeKey.startsWith(request.getPrefix())) {
                            return null;
                        }
                        if (request.getMarker() != null && relativeKey.compareTo(request.getMarker()) <= 0) {
                            return null;
                        }
                        try {
                            String eTag = DigestUtils.md5DigestAsHex(Files.newInputStream(path));
                            long size = Files.size(path);
                            long lastModified = Files.getLastModifiedTime(path).toMillis();
                            return S3ObjectInfo.builder().key(relativeKey).size(size).eTag(eTag).lastModified(lastModified).build();
                        } catch (IOException e) {
                            return null;
                        }
                    })
                    .filter(obj -> obj != null)
                    .sorted((o1, o2) -> o1.getKey().compareTo(o2.getKey()))
                    .limit(request.getMaxKeys())
                    .forEach(result::add);
        } catch (IOException e) {
            throw new RuntimeException("Failed to list objects", e);
        }
        return result;
    }

    @Override
    public String multipartPartObjectKey(String uploadId, int partNumber) {
        return MULTIPART_TEMP_PREFIX + uploadId + "/" + partNumber;
    }

    @Override
    public List<MultipartPartRecord> listMultipartParts(String bucket, String uploadId) {
        Path dir = Paths.get(basePath, bucket, "objects", ".s3multipart", uploadId).normalize();
        if (!Files.isDirectory(dir)) {
            return List.of();
        }
        List<MultipartPartRecord> out = new ArrayList<>();
        try (Stream<Path> stream = Files.list(dir)) {
            stream.filter(Files::isRegularFile).forEach(path -> {
                String name = path.getFileName().toString();
                if (!name.matches("\\d+")) {
                    return;
                }
                int partNumber = Integer.parseInt(name);
                String storageKey = multipartPartObjectKey(uploadId, partNumber);
                try {
                    long size = Files.size(path);
                    String eTag = DigestUtils.md5DigestAsHex(Files.newInputStream(path));
                    out.add(MultipartPartRecord.builder()
                            .partNumber(partNumber)
                            .storageKey(storageKey)
                            .size(size)
                            .eTag(eTag)
                            .build());
                } catch (IOException e) {
                    throw new RuntimeException("Failed to read multipart part: " + storageKey, e);
                }
            });
        } catch (IOException e) {
            throw new RuntimeException("Failed to list multipart parts for uploadId=" + uploadId, e);
        }
        return out;
    }

    @Override
    public MultipartComposeResult composeMultipartParts(String bucket, String destinationKey, List<String> partKeysInOrder) {
        if (partKeysInOrder == null || partKeysInOrder.isEmpty()) {
            throw new IllegalArgumentException("partKeysInOrder must not be empty");
        }
        try {
            Path destPath = getObjectFilePath(bucket, destinationKey);
            Files.createDirectories(destPath.getParent());
            try (OutputStream out = Files.newOutputStream(destPath)) {
                for (String partKey : partKeysInOrder) {
                    Path partPath = getObjectFilePath(bucket, partKey);
                    if (!Files.exists(partPath)) {
                        throw new FileNotFoundException("Part not found: " + partKey);
                    }
                    try (InputStream in = Files.newInputStream(partPath)) {
                        byte[] buffer = new byte[8192];
                        int read;
                        while ((read = in.read(buffer)) != -1) {
                            out.write(buffer, 0, read);
                        }
                    }
                }
            }
            long size = Files.size(destPath);
            try (InputStream inputStream = Files.newInputStream(destPath)) {
                String eTag = DigestUtils.md5DigestAsHex(inputStream);
                return MultipartComposeResult.builder().eTag(eTag).size(size).build();
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to compose multipart parts", e);
        }
    }

    private Path getObjectMetaPath(String bucket, String key) {
        return Paths.get(basePath, bucket, ".s3meta", "objects", key + ".json");
    }

    private Path getMultipartUploadPath(String bucket, String uploadId) {
        return Paths.get(basePath, bucket, ".s3meta", "multipart", uploadId + ".json");
    }

    private Path getObjectFilePath(String bucket, String key) {
        Path safeKey = Paths.get(key).normalize();
        if (safeKey.isAbsolute()) {
            throw new IllegalArgumentException("Invalid key: " + key);
        }
        return Paths.get(basePath, bucket, "objects", safeKey.toString());
    }

    private Path getObjectsRootPath(String bucket) {
        return Paths.get(basePath, bucket, "objects");
    }

    private void deleteDirectoryRecursively(Path path) throws IOException {
        if (Files.isDirectory(path)) {
            try (Stream<Path> stream = Files.list(path)) {
                for (Path child : stream.toList()) {
                    deleteDirectoryRecursively(child);
                }
            }
        }
        Files.deleteIfExists(path);
    }
}
