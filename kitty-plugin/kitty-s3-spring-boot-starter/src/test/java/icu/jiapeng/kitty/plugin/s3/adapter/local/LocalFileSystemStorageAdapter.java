package icu.jiapeng.kitty.plugin.s3.adapter.local;

import icu.jiapeng.kitty.plugin.s3.config.S3Properties;
import icu.jiapeng.kitty.plugin.s3.model.dto.ListObjectsRequest;
import icu.jiapeng.kitty.plugin.s3.model.dto.MultipartComposeResult;
import icu.jiapeng.kitty.plugin.s3.model.dto.PutObjectResult;
import icu.jiapeng.kitty.plugin.s3.port.StorageBackendPort;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * 本地文件系统存储后端实现
 * 所有数据持久化到磁盘，重启不丢失
 * 存储结构：
 * basePath/
 *   {bucket}/
 *     objects/          # 实际对象存储
 *       {key}           # 内容文件
 *     .s3meta/           # 元数据目录
 *       objects/
 *         {key}.json     # 对象元数据
 *       multipart/
 *         {uploadId}.json # 分块上传元数据
 */
@Component
@ConditionalOnProperty(prefix = "kitty.s3.storage", name = "type", havingValue = "local", matchIfMissing = true)
public class LocalFileSystemStorageAdapter implements StorageBackendPort {

    private static final String MULTIPART_TEMP_PREFIX = ".s3multipart/";

    private final String basePath;

    public LocalFileSystemStorageAdapter(S3Properties properties) {
        this.basePath = properties.getStorage().getLocal().getBasePath();
        File rootDir = new File(basePath);
        if (!rootDir.exists()) {
            rootDir.mkdirs();
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
                return PutObjectResult.builder()
                        .eTag(eTag)
                        .size(actualSize)
                        .build();
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
            Path filePath = getObjectFilePath(bucket, key);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete object", e);
        }
    }

    @Override
    public boolean exists(String bucket, String key) {
        Path filePath = getObjectFilePath(bucket, key);
        return Files.exists(filePath);
    }

    @Override
    public List<ObjectInfo> listObjects(ListObjectsRequest request) {
        List<ObjectInfo> result = new ArrayList<>();
        Path objectsPath = getObjectsRootPath(request.getBucketName());
        
        if (!Files.exists(objectsPath)) {
            return result;
        }

        try {
            Files.walk(objectsPath)
                    .filter(path -> Files.isRegularFile(path))
                    .map(path -> {
                        String relativeKey = objectsPath.relativize(path).toString();
                        relativeKey = relativeKey.replace('\\', '/');
                        // 隐藏分块上传的临时分片文件，避免对外暴露
                        if (relativeKey.startsWith(MULTIPART_TEMP_PREFIX)) {
                            return null;
                        }
                        if (request.getPrefix() != null && !relativeKey.startsWith(request.getPrefix())) {
                            return null;
                        }
                        
                        // 处理marker - 如果设置了marker，只返回key大于marker的对象
                        if (request.getMarker() != null && relativeKey.compareTo(request.getMarker()) <= 0) {
                            return null;
                        }
                        
                        try {
                            String eTag = DigestUtils.md5DigestAsHex(Files.newInputStream(path));
                            long size = Files.size(path);
                            long lastModified = Files.getLastModifiedTime(path).toMillis();
                            
                            return new ObjectInfo(relativeKey, size, eTag, lastModified);
                        } catch (IOException e) {
                            return null;
                        }
                    })
                    .filter(obj -> obj != null)
                    .sorted((o1, o2) -> o1.getKey().compareTo(o2.getKey())) // 按key排序
                    .limit(request.getMaxKeys())
                    .forEach(result::add);
        } catch (IOException e) {
            throw new RuntimeException("Failed to list objects", e);
        }

        return result;
    }

    @Override
    public List<ObjectInfo> listMultipartParts(String bucket, String uploadId) {
        Path dir = Paths.get(basePath, bucket, "objects", ".s3multipart", uploadId).normalize();
        if (!Files.isDirectory(dir)) {
            return List.of();
        }
        List<ObjectInfo> out = new ArrayList<>();
        try (Stream<Path> stream = Files.list(dir)) {
            stream.filter(Files::isRegularFile).forEach(path -> {
                String name = path.getFileName().toString();
                if (!name.matches("\\d+")) {
                    return;
                }
                String key = MULTIPART_TEMP_PREFIX + uploadId + "/" + name;
                try {
                    long size = Files.size(path);
                    String eTag = DigestUtils.md5DigestAsHex(Files.newInputStream(path));
                    long lastModified = Files.getLastModifiedTime(path).toMillis();
                    out.add(new ObjectInfo(key, size, eTag, lastModified));
                } catch (IOException e) {
                    throw new RuntimeException("Failed to read multipart part: " + key, e);
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

    /**
     * 获取对象实际存储路径
     */
    private Path getObjectFilePath(String bucket, String key) {
        Path safeKey = Paths.get(key).normalize();
        if (safeKey.isAbsolute()) {
            throw new IllegalArgumentException("Invalid key: " + key);
        }
        return Paths.get(basePath, bucket, "objects", safeKey.toString());
    }

    /**
     * 获取对象根目录路径
     */
    private Path getObjectsRootPath(String bucket) {
        return Paths.get(basePath, bucket, "objects");
    }
}
