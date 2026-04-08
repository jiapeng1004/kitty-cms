package icu.jiapeng.kitty.plugin.s3.service;

import icu.jiapeng.kitty.plugin.s3.domain.MultipartUpload;
import icu.jiapeng.kitty.plugin.s3.domain.S3Object;
import icu.jiapeng.kitty.plugin.s3.model.dto.ListObjectsRequest;
import icu.jiapeng.kitty.plugin.s3.model.dto.MultipartComposeResult;
import icu.jiapeng.kitty.plugin.s3.model.dto.MultipartPartRecord;
import icu.jiapeng.kitty.plugin.s3.model.dto.PutObjectRequest;
import icu.jiapeng.kitty.plugin.s3.model.dto.PutObjectResult;
import icu.jiapeng.kitty.plugin.s3.model.dto.S3ObjectInfo;
import icu.jiapeng.kitty.plugin.s3.model.dto.UploadPartRequest;
import icu.jiapeng.kitty.plugin.s3.model.request.CompleteMultipartUploadRequest;
import icu.jiapeng.kitty.plugin.s3.model.request.DeleteObjectsRequest;
import icu.jiapeng.kitty.plugin.s3.util.JaxbXmlMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * 默认 S3 协议编排实现：依赖单一 {@link S3Service}，不直接处理底层流细节。
 */
@Service
@AllArgsConstructor
public class DefaultS3ObjectService implements S3ObjectService {

    private final S3Service s3;
    private final JaxbXmlMapper xmlMapper;

    @Override
    public List<String> listBuckets() {
        return s3.listBuckets();
    }

    @Override
    public void createBucket(String bucketName, String owner) {
        if (s3.bucketExists(bucketName)) {
            throw new IllegalArgumentException("Bucket already exists: " + bucketName);
        }
        s3.saveBucket(bucketName, owner);
    }

    @Override
    public void deleteBucket(String bucketName) {
        if (!s3.bucketExists(bucketName)) {
            throw new IllegalArgumentException("Bucket not found: " + bucketName);
        }
        s3.deleteBucket(bucketName);
    }

    @Override
    public boolean objectExists(String bucketName, String key) {
        if (!s3.bucketExists(bucketName)) {
            return false;
        }
        return s3.exists(bucketName, key);
    }

    @Override
    public Optional<S3Object> getObjectMetadata(String bucketName, String key) {
        if (!s3.bucketExists(bucketName)) {
            return Optional.empty();
        }
        return s3.getObjectMetadata(bucketName, key);
    }

    @Override
    public String putObject(PutObjectRequest request) {
        if (!s3.bucketExists(request.getBucketName())) {
            throw new IllegalArgumentException("Bucket not found: " + request.getBucketName());
        }

        PutObjectResult result = s3.putObject(
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
        s3.saveObjectMetadata(s3Object);

        return eTag;
    }

    @Override
    public Optional<S3Object> getObject(String bucketName, String key) {
        if (!s3.bucketExists(bucketName)) {
            return Optional.empty();
        }

        Optional<S3Object> metadataOpt = s3.getObjectMetadata(bucketName, key);
        if (metadataOpt.isEmpty()) {
            return Optional.empty();
        }

        S3Object s3Object = metadataOpt.get();
        final String bucket = bucketName;
        final String objectKey = key;
        s3Object.setContentStreamSupplier(() -> s3.getObject(bucket, objectKey));

        return Optional.of(s3Object);
    }

    @Override
    public void deleteObject(String bucketName, String key) {
        if (!s3.bucketExists(bucketName)) {
            return;
        }

        s3.deleteObjectMetadata(bucketName, key);
        s3.deleteObject(bucketName, key);
    }

    @Override
    public List<String> deleteObjects(String bucketName, String keyPrefix, String requestBody) {
        if (!s3.bucketExists(bucketName)) {
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
        if (!s3.bucketExists(bucketName)) {
            throw new IllegalArgumentException("Bucket not found: " + bucketName);
        }

        String uploadId = UUID.randomUUID().toString().replace("-", "");
        MultipartUpload upload = new MultipartUpload(uploadId, bucketName, key, contentType);
        s3.saveMultipartUpload(upload);

        return uploadId;
    }

    @Override
    public String uploadPart(UploadPartRequest request) {
        Optional<MultipartUpload> uploadOpt = s3.getMultipartUpload(request.getUploadId());
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

        String partKey = s3.multipartPartObjectKey(request.getUploadId(), request.getPartNumber());

        PutObjectResult result = s3.putObject(upload.getBucketName(), partKey, request.getContent(), request.getSize());
        return result.getETag();
    }

    @Override
    public String completeMultipartUpload(String bucketName, String key, String uploadId, String requestBody) {
        Optional<MultipartUpload> uploadOpt = s3.getMultipartUpload(uploadId);
        if (uploadOpt.isEmpty()) {
            throw new IllegalArgumentException("Upload not found: " + uploadId);
        }

        MultipartUpload upload = uploadOpt.get();
        if (!upload.getBucketName().equals(bucketName) || !upload.getKey().equals(key)) {
            throw new IllegalArgumentException("Bucket or key does not match multipart upload session");
        }
        String contentType = upload.getContentType();

        List<MultipartPartRecord> onDisk = s3.listMultipartParts(bucketName, uploadId);
        Map<Integer, MultipartPartRecord> diskByPart = new HashMap<>(onDisk.size());
        for (MultipartPartRecord r : onDisk) {
            diskByPart.put(r.getPartNumber(), r);
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
            MultipartPartRecord info = diskByPart.get(partNumber);
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
            partKeysInOrder.add(info.getStorageKey());
            expectedMergedSize += info.getSize();
        }

        MultipartComposeResult composed = s3.composeMultipartParts(bucketName, key, partKeysInOrder);
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
        s3.saveObjectMetadata(s3Object);

        for (MultipartPartRecord o : onDisk) {
            s3.deleteObject(bucketName, o.getStorageKey());
        }

        s3.deleteMultipartUpload(uploadId);

        return eTag;
    }

    @Override
    public void abortMultipartUpload(String uploadId) {
        Optional<MultipartUpload> uploadOpt = s3.getMultipartUpload(uploadId);
        if (uploadOpt.isEmpty()) {
            throw new IllegalArgumentException("Upload not found: " + uploadId);
        }

        MultipartUpload upload = uploadOpt.get();
        for (MultipartPartRecord o : s3.listMultipartParts(upload.getBucketName(), uploadId)) {
            s3.deleteObject(upload.getBucketName(), o.getStorageKey());
        }

        s3.deleteMultipartUpload(uploadId);
    }

    @Override
    public List<S3ObjectInfo> listObjects(ListObjectsRequest request) {
        return s3.listObjects(request);
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
