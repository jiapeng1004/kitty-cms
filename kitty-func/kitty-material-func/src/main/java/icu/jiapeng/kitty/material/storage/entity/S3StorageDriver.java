package icu.jiapeng.kitty.material.storage.entity;

import cn.hutool.core.util.StrUtil;
import icu.jiapeng.kitty.common.core.util.PathUtil;
import icu.jiapeng.kitty.material.resource.constants.FileEngineTypeEnum;
import icu.jiapeng.kitty.material.resource.entity.KtFileStorage;
import icu.jiapeng.kitty.material.storage.StorageConnectivityResult;
import icu.jiapeng.kitty.material.storage.StorageDriver;
import icu.jiapeng.kitty.material.storage.StorageMimeTypes;
import icu.jiapeng.kitty.material.storage.StoragePartEtag;
import icu.jiapeng.kitty.material.storage.StorageRouteRequest;
import icu.jiapeng.kitty.material.storage.StorageRouteResult;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.AbortMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CompletedMultipartUpload;
import software.amazon.awssdk.services.s3.model.CompletedPart;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.UploadPartRequest;
import software.amazon.awssdk.services.s3.model.UploadPartResponse;
import software.amazon.awssdk.services.s3.S3Configuration;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * S3 存储驱动（路由契约实现；分片上传走 Multipart，不落本地合并文件）。
 */
@Component
public class S3StorageDriver implements StorageDriver {

    @Override
    public String driverName() {
        return FileEngineTypeEnum.OBJECT_STORAGE.getType();
    }

    @Override
    public void deleteObject(KtFileStorage storageConfig, String objectKey) throws IOException {
        if (!isValidObjectKey(objectKey)) {
            return;
        }
        try (S3Client client = buildClient(storageConfig)) {
            client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(storageConfig.getBucket().trim())
                    .key(objectKey)
                    .build());
        }
    }

    @Override
    public void putEmptyObject(KtFileStorage storageConfig, String objectKey, String contentTypeOrNull) throws IOException {
        String ct = effectiveContentType(contentTypeOrNull, objectKey);
        try (S3Client client = buildClient(storageConfig)) {
            PutObjectRequest.Builder b = PutObjectRequest.builder()
                    .bucket(storageConfig.getBucket().trim())
                    .key(objectKey)
                    .contentType(ct);
            client.putObject(b.build(), RequestBody.fromBytes(new byte[0]));
        }
    }

    @Override
    public void writeSequentialLocalPartFilesToObject(KtFileStorage storageConfig, String objectKey, List<Path> orderedLocalPartPaths) throws IOException {
        throw new IOException("sequential merge from local part files is not supported for object storage");
    }

    @Override
    public String initiateMultipartUpload(KtFileStorage storageConfig, String objectKey, String contentTypeOrNull) throws IOException {
        String ct = effectiveContentType(contentTypeOrNull, objectKey);
        try (S3Client client = buildClient(storageConfig)) {
            return client.createMultipartUpload(CreateMultipartUploadRequest.builder()
                    .bucket(storageConfig.getBucket().trim())
                    .key(objectKey)
                    .contentType(ct)
                    .build()).uploadId();
        }
    }

    /**
     * 显式 Content-Type，便于浏览器对预签名 URL / 直链预览；缺省按对象键扩展名推断。
     */
    private static String effectiveContentType(String contentTypeOrNull, String objectKey) {
        if (StringUtils.hasText(contentTypeOrNull)) {
            return contentTypeOrNull.trim();
        }
        return StorageMimeTypes.resolveFromTitleAndObjectKey(null, objectKey);
    }

    @Override
    public String uploadMultipartPart(KtFileStorage storageConfig, String objectKey, String uploadId, int partNumber, byte[] data) throws IOException {
        byte[] payload = data == null ? new byte[0] : data;
        try (S3Client client = buildClient(storageConfig)) {
            UploadPartRequest req = UploadPartRequest.builder()
                    .bucket(storageConfig.getBucket().trim())
                    .key(objectKey)
                    .uploadId(uploadId)
                    .partNumber(partNumber)
                    .contentLength((long) payload.length)
                    .build();
            UploadPartResponse resp = client.uploadPart(req, RequestBody.fromBytes(payload));
            String etag = resp.eTag();
            return etag == null ? "" : etag;
        }
    }

    @Override
    public void completeMultipartUpload(KtFileStorage storageConfig, String objectKey, String uploadId, List<StoragePartEtag> parts) throws IOException {
        List<StoragePartEtag> sorted = new ArrayList<>(parts);
        sorted.sort(Comparator.comparingInt(StoragePartEtag::partNumber));
        List<CompletedPart> completedParts = new ArrayList<>(sorted.size());
        for (StoragePartEtag p : sorted) {
            completedParts.add(CompletedPart.builder()
                    .partNumber(p.partNumber())
                    .eTag(p.eTag())
                    .build());
        }
        try (S3Client client = buildClient(storageConfig)) {
            client.completeMultipartUpload(CompleteMultipartUploadRequest.builder()
                    .bucket(storageConfig.getBucket().trim())
                    .key(objectKey)
                    .uploadId(uploadId)
                    .multipartUpload(CompletedMultipartUpload.builder().parts(completedParts).build())
                    .build());
        }
    }

    @Override
    public void abortMultipartUpload(KtFileStorage storageConfig, String objectKey, String uploadId) throws IOException {
        try (S3Client client = buildClient(storageConfig)) {
            client.abortMultipartUpload(AbortMultipartUploadRequest.builder()
                    .bucket(storageConfig.getBucket().trim())
                    .key(objectKey)
                    .uploadId(uploadId)
                    .build());
        }
    }

    private static S3Client buildClient(KtFileStorage storageConfig) throws IOException {
        if (StrUtil.isBlank(storageConfig.getBucket())) {
            throw new IOException("object storage bucket missing");
        }
        String endpoint = StrUtil.firstNonBlank(storageConfig.getInternalEndpoint(), storageConfig.getExternalEndpoint());
        if (StrUtil.isBlank(endpoint)) {
            throw new IOException("object storage endpoint missing");
        }
        if (StrUtil.isBlank(storageConfig.getAccessKey()) || StrUtil.isBlank(storageConfig.getSecretKey())) {
            throw new IOException("object storage credentials missing");
        }
        return S3Client.builder()
                .endpointOverride(URI.create(endpoint.trim()))
                .region(Region.US_EAST_1)
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(
                                storageConfig.getAccessKey().trim(),
                                storageConfig.getSecretKey().trim())))
                .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build())
                .build();
    }

    @Override
    public StorageRouteResult route(StorageRouteRequest request, KtFileStorage storageConfig) {
        String normalized = normalizeObjectKey(request.getObjectKey());
        StorageRouteResult result = new StorageRouteResult();
        result.setStorageId(request.getStorageId());
        result.setDriverName(driverName());
        result.setObjectKey(normalized);
        String configPart = StrUtil.firstNonBlank(storageConfig.getExternalEndpoint(), storageConfig.getInternalEndpoint());
        String path = PathUtil.builderPath(configPart, normalized);
        result.setRouteTarget("s3://" + path);
        return result;
    }

    @Override
    public String normalizeObjectKey(String objectKey) {
        if (objectKey == null) {
            return "";
        }
        String normalized = objectKey.trim().replace("\\", "/");
        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        return normalized.replaceAll("/+", "/");
    }

    @Override
    public StorageConnectivityResult testConnectivity(String storageId, KtFileStorage storageConfig) {
        StorageConnectivityResult result = new StorageConnectivityResult();
        result.setStorageId(storageId);
        result.setDriverName(driverName());
        String endpoint = storageConfig.getInternalEndpoint();
        if (endpoint == null || endpoint.isBlank()) {
            result.setReachable(false);
            result.setDetail("missing endpoint in storage config");
            return result;
        }
        try {
            HttpURLConnection conn = (HttpURLConnection) URI.create(endpoint).toURL().openConnection();
            conn.setConnectTimeout(2000);
            conn.setReadTimeout(3000);
            conn.setRequestMethod("GET");
            int code = conn.getResponseCode();
            result.setReachable(code >= 200 && code < 500);
            result.setDetail("endpoint=" + endpoint + ", httpStatus=" + code);
            return result;
        } catch (Exception e) {
            result.setReachable(false);
            result.setDetail("endpoint=" + endpoint + ", error=" + e.getClass().getSimpleName());
            return result;
        }
    }
}
