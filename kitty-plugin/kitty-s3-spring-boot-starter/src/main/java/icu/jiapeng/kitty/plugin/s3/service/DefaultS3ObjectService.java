package icu.jiapeng.kitty.plugin.s3.service;

import icu.jiapeng.kitty.plugin.s3.domain.MultipartUpload;
import icu.jiapeng.kitty.plugin.s3.domain.S3Object;
import icu.jiapeng.kitty.plugin.s3.model.dto.ListObjectsRequest;
import icu.jiapeng.kitty.plugin.s3.model.dto.MultipartComposeResult;
import icu.jiapeng.kitty.plugin.s3.model.dto.PutObjectRequest;
import icu.jiapeng.kitty.plugin.s3.model.dto.PutObjectResult;
import icu.jiapeng.kitty.plugin.s3.model.dto.UploadPartRequest;
import icu.jiapeng.kitty.plugin.s3.model.request.CompleteMultipartUploadRequest;
import icu.jiapeng.kitty.plugin.s3.model.request.DeleteObjectsRequest;
import icu.jiapeng.kitty.plugin.s3.port.MetadataStorePort;
import icu.jiapeng.kitty.plugin.s3.port.StorageBackendPort;
import icu.jiapeng.kitty.plugin.s3.util.JaxbXmlMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * 默认 S3 协议编排实现：调度元数据与存储端口，不直接处理底层流细节。
 */
@Service
@AllArgsConstructor
public class DefaultS3ObjectService implements S3ObjectService {

    private static final String MULTIPART_TEMP_PREFIX = ".s3multipart";

    private final StorageBackendPort storageBackend;
    private final MetadataStorePort metadataStore;
    private final JaxbXmlMapper xmlMapper;

    @Override
    public List<String> listBuckets() {
        return metadataStore.listBuckets();
    }

    @Override
    public void createBucket(String bucketName, String owner) {
        if (metadataStore.bucketExists(bucketName)) {
            throw new IllegalArgumentException("Bucket already exists: " + bucketName);
        }
        metadataStore.saveBucket(bucketName, owner);
    }

    @Override
    public void deleteBucket(String bucketName) {
        if (!metadataStore.bucketExists(bucketName)) {
            throw new IllegalArgumentException("Bucket not found: " + bucketName);
        }
        metadataStore.deleteBucket(bucketName);
    }

    @Override
    public boolean objectExists(String bucketName, String key) {
        if (!metadataStore.bucketExists(bucketName)) {
            return false;
        }
        return storageBackend.exists(bucketName, key);
    }

    @Override
    public Optional<S3Object> getObjectMetadata(String bucketName, String key) {
        if (!metadataStore.bucketExists(bucketName)) {
            return Optional.empty();
        }
        return metadataStore.getObjectMetadata(bucketName, key);
    }

    @Override
    public String putObject(PutObjectRequest request) {
        if (!metadataStore.bucketExists(request.getBucketName())) {
            throw new IllegalArgumentException("Bucket not found: " + request.getBucketName());
        }

        PutObjectResult result = storageBackend.putObject(
                request.getBucketName(),
                request.getKey(),
                request.getContent(),
                request.getSize()
        );
        String eTag = result.getETag();
        long actualSize = result.getSize();

        S3Object s3Object = new S3Object();
        s3Object.setBucketName(request.getBucketName());
        s3Object.setKey(request.getKey());
        s3Object.setSize(actualSize);
        s3Object.setContentType(request.getContentType());
        s3Object.setETag(eTag);
        s3Object.setLastModified(new Date());
        s3Object.setMetadata(request.getMetadata() == null ? new HashMap<>() : new HashMap<>(request.getMetadata()));
        metadataStore.saveObjectMetadata(s3Object);

        return eTag;
    }

    @Override
    public Optional<S3Object> getObject(String bucketName, String key) {
        if (!metadataStore.bucketExists(bucketName)) {
            return Optional.empty();
        }

        Optional<S3Object> metadataOpt = metadataStore.getObjectMetadata(bucketName, key);
        if (metadataOpt.isEmpty()) {
            return Optional.empty();
        }

        S3Object s3Object = metadataOpt.get();
        InputStream content = storageBackend.getObject(bucketName, key);
        s3Object.setContent(content);

        return Optional.of(s3Object);
    }

    @Override
    public void deleteObject(String bucketName, String key) {
        if (!metadataStore.bucketExists(bucketName)) {
            return;
        }

        metadataStore.deleteObjectMetadata(bucketName, key);
        storageBackend.deleteObject(bucketName, key);
    }

    @Override
    public List<String> deleteObjects(String bucketName, String keyPrefix, String requestBody) {
        if (!metadataStore.bucketExists(bucketName)) {
            return List.of();
        }

        List<String> keysToDelete = parseDeleteRequest(requestBody);
        if (keysToDelete.isEmpty()) {
            return List.of();
        }

        String normalizedPrefix = normalizePrefix(keyPrefix);
        List<String> deletedKeys = new ArrayList<>(keysToDelete.size());
        keysToDelete.forEach(key -> {
            String fullKey = normalizedPrefix.isEmpty() ? key : normalizedPrefix + "/" + key;
            deleteObject(bucketName, fullKey);
            deletedKeys.add(fullKey);
        });

        return deletedKeys;
    }

    @Override
    public String initiateMultipartUpload(String bucketName, String key, String contentType) {
        if (!metadataStore.bucketExists(bucketName)) {
            throw new IllegalArgumentException("Bucket not found: " + bucketName);
        }

        String uploadId = UUID.randomUUID().toString().replace("-", "");
        MultipartUpload upload = new MultipartUpload(uploadId, bucketName, key, contentType);
        metadataStore.saveMultipartUpload(upload);

        return uploadId;
    }

    @Override
    public String uploadPart(UploadPartRequest request) {
        Optional<MultipartUpload> uploadOpt = metadataStore.getMultipartUpload(request.getUploadId());
        if (uploadOpt.isEmpty()) {
            throw new IllegalArgumentException("Upload not found: " + request.getUploadId());
        }

        MultipartUpload upload = uploadOpt.get();
        if (StringUtils.hasText(request.getBucketName()) && !request.getBucketName().equals(upload.getBucketName())) {
            throw new IllegalArgumentException("Bucket does not match multipart upload session");
        }
        if (StringUtils.hasText(request.getObjectKey()) && !request.getObjectKey().equals(upload.getKey())) {
            throw new IllegalArgumentException("Object key does not match multipart upload session");
        }

        String partKey = buildMultipartPartKey(upload, request.getPartNumber());

        PutObjectResult result = storageBackend.putObject(upload.getBucketName(), partKey, request.getContent(), request.getSize());
        return result.getETag();
    }

    @Override
    public String completeMultipartUpload(String bucketName, String key, String uploadId, String requestBody) {
        Optional<MultipartUpload> uploadOpt = metadataStore.getMultipartUpload(uploadId);
        if (uploadOpt.isEmpty()) {
            throw new IllegalArgumentException("Upload not found: " + uploadId);
        }

        MultipartUpload upload = uploadOpt.get();
        if (!upload.getBucketName().equals(bucketName) || !upload.getKey().equals(key)) {
            throw new IllegalArgumentException("Bucket or key does not match multipart upload session");
        }
        String contentType = upload.getContentType();

        List<StorageBackendPort.ObjectInfo> onDisk = storageBackend.listMultipartParts(bucketName, uploadId);
        Map<Integer, StorageBackendPort.ObjectInfo> diskByPart = new HashMap<>(onDisk.size());
        for (StorageBackendPort.ObjectInfo o : onDisk) {
            diskByPart.put(parsePartNumberFromMultipartKey(o.getKey()), o);
        }

        List<CompletePart> requestedParts = parseCompleteMultipartRequest(requestBody);
        if (requestedParts.isEmpty()) {
            requestedParts = diskByPart.keySet().stream()
                    .sorted()
                    .map(pn -> new CompletePart(pn, diskByPart.get(pn).getETag()))
                    .toList();
        }

        if (requestedParts.isEmpty()) {
            throw new IllegalStateException("No parts uploaded for uploadId: " + uploadId);
        }

        List<String> partKeysInOrder = new ArrayList<>(requestedParts.size());
        long expectedMergedSize = 0L;
        for (CompletePart completePart : requestedParts) {
            int partNumber = completePart.partNumber();
            StorageBackendPort.ObjectInfo info = diskByPart.get(partNumber);
            if (info == null) {
                throw new IllegalArgumentException("Missing uploaded part: " + partNumber);
            }
            if (StringUtils.hasText(completePart.eTag())) {
                String expectedETag = normalizeETag(completePart.eTag());
                String actualETag = normalizeETag(info.getETag());
                if (!Objects.equals(expectedETag, actualETag)) {
                    throw new IllegalArgumentException("ETag mismatch for part " + partNumber);
                }
            }
            partKeysInOrder.add(buildMultipartPartKey(upload, partNumber));
            expectedMergedSize += info.getSize();
        }

        MultipartComposeResult composed = storageBackend.composeMultipartParts(bucketName, key, partKeysInOrder);
        String eTag = composed.getETag();
        long actualMergedSize = composed.getSize();
        if (actualMergedSize != expectedMergedSize) {
            throw new IllegalStateException("Multipart compose size mismatch, expected="
                    + expectedMergedSize + ", actual=" + actualMergedSize);
        }

        S3Object s3Object = new S3Object();
        s3Object.setBucketName(bucketName);
        s3Object.setKey(key);
        s3Object.setSize(actualMergedSize);
        s3Object.setContentType(contentType);
        s3Object.setETag(eTag);
        s3Object.setLastModified(new Date());
        s3Object.setMetadata(new HashMap<>());
        metadataStore.saveObjectMetadata(s3Object);

        for (StorageBackendPort.ObjectInfo o : onDisk) {
            storageBackend.deleteObject(bucketName, o.getKey());
        }

        metadataStore.deleteMultipartUpload(uploadId);

        return eTag;
    }

    @Override
    public void abortMultipartUpload(String uploadId) {
        Optional<MultipartUpload> uploadOpt = metadataStore.getMultipartUpload(uploadId);
        if (uploadOpt.isEmpty()) {
            throw new IllegalArgumentException("Upload not found: " + uploadId);
        }

        MultipartUpload upload = uploadOpt.get();
        for (StorageBackendPort.ObjectInfo o : storageBackend.listMultipartParts(upload.getBucketName(), uploadId)) {
            storageBackend.deleteObject(upload.getBucketName(), o.getKey());
        }

        metadataStore.deleteMultipartUpload(uploadId);
    }

    @Override
    public List<StorageBackendPort.ObjectInfo> listObjects(ListObjectsRequest request) {
        return storageBackend.listObjects(request);
    }

    private List<String> parseDeleteRequest(String requestBody) {
        if (!StringUtils.hasText(requestBody)) {
            return List.of();
        }
        try {
            DeleteObjectsRequest request = xmlMapper.readValue(requestBody, DeleteObjectsRequest.class);
            if (request == null || request.getObjects() == null || request.getObjects().isEmpty()) {
                return List.of();
            }
            List<String> keys = new ArrayList<>(request.getObjects().size());
            for (DeleteObjectsRequest.ObjectItem objectItem : request.getObjects()) {
                if (objectItem == null) {
                    continue;
                }
                String key = objectItem.getKey();
                if (StringUtils.hasText(key)) {
                    keys.add(key.trim());
                }
            }
            return keys;
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid DeleteObjects request body", e);
        }
    }

    private String normalizePrefix(String keyPrefix) {
        if (!StringUtils.hasText(keyPrefix)) {
            return "";
        }
        return keyPrefix.endsWith("/") ? keyPrefix.substring(0, keyPrefix.length() - 1) : keyPrefix;
    }

    private String buildMultipartPartKey(MultipartUpload upload, int partNumber) {
        return MULTIPART_TEMP_PREFIX + "/" + upload.getUploadId() + "/" + partNumber;
    }

    private int parsePartNumberFromMultipartKey(String key) {
        int idx = key.lastIndexOf('/');
        if (idx < 0 || idx < 1) {
            throw new IllegalArgumentException("Invalid multipart part key: " + key);
        }
        return Integer.parseInt(key.substring(idx + 1));
    }

    private List<CompletePart> parseCompleteMultipartRequest(String requestBody) {
        if (!StringUtils.hasText(requestBody)) {
            return List.of();
        }
        try {
            CompleteMultipartUploadRequest request = xmlMapper.readValue(requestBody, CompleteMultipartUploadRequest.class);
            if (request == null || request.getParts() == null || request.getParts().isEmpty()) {
                return List.of();
            }

            List<CompletePart> parts = new ArrayList<>(request.getParts().size());
            for (CompleteMultipartUploadRequest.PartItem partItem : request.getParts()) {
                if (partItem == null || partItem.getPartNumber() == null) {
                    throw new IllegalArgumentException("Invalid CompleteMultipartUpload: missing PartNumber");
                }
                int partNumber = partItem.getPartNumber();
                String eTag = partItem.getETag();
                parts.add(new CompletePart(partNumber, eTag));
            }
            return parts;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid CompleteMultipartUpload request body", e);
        }
    }

    private String normalizeETag(String eTag) {
        if (!StringUtils.hasText(eTag)) {
            return eTag;
        }
        String normalized = eTag.trim();
        if (normalized.startsWith("\"") && normalized.endsWith("\"") && normalized.length() >= 2) {
            normalized = normalized.substring(1, normalized.length() - 1);
        }
        return normalized;
    }

    private record CompletePart(int partNumber, String eTag) {
    }
}
