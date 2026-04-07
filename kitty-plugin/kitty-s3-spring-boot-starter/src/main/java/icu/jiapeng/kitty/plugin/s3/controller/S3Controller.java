package icu.jiapeng.kitty.plugin.s3.controller;

import icu.jiapeng.kitty.plugin.s3.domain.S3Object;
import icu.jiapeng.kitty.plugin.s3.model.dto.ListObjectsRequest;
import icu.jiapeng.kitty.plugin.s3.model.dto.PutObjectRequest;
import icu.jiapeng.kitty.plugin.s3.model.dto.UploadPartRequest;
import icu.jiapeng.kitty.plugin.s3.model.response.*;
import icu.jiapeng.kitty.plugin.s3.port.StorageBackendPort;
import icu.jiapeng.kitty.plugin.s3.service.S3ObjectService;
import icu.jiapeng.kitty.plugin.s3.util.JaxbXmlMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static org.springframework.web.bind.annotation.RequestMethod.HEAD;

/**
 * S3标准API Controller
 * 实现AWS S3 REST API规范
 */
@RestController
@RequestMapping("/s3")
@AllArgsConstructor
public class S3Controller {

    private final S3ObjectService s3ObjectService;
    private final JaxbXmlMapper xmlMapper;

    /**
     * GET / - 列出所有存储桶
     */
//    @RequestMapping(value = {"/", ""}, method = RequestMethod.GET)
    @GetMapping(value = {"/", ""})
    public ResponseEntity<String> listBuckets() {
        List<String> buckets = s3ObjectService.listBuckets();

        ListBucketsResponse response = ListBucketsResponse.builder()
                .buckets(ListBucketsResponse.Buckets.builder()
                        .bucketList(buckets.stream()
                                .map(bucket -> ListBucketsResponse.Bucket.builder()
                                        .name(bucket)
                                        .creationDate(new Date())
                                        .build())
                                .toList())
                        .build())
                .build();

        try {
            String xml = xmlMapper.writeValueAsString(response);
            return ResponseEntity.status(HttpStatus.OK)
                    .contentType(MediaType.APPLICATION_XML)
                    .body(xml);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * PUT /{bucket} - 创建存储桶
     */
    @PutMapping(value = {"/{bucket}", "/{bucket}/"})
    public ResponseEntity<Void> createBucket(@PathVariable String bucket) {
        s3ObjectService.createBucket(bucket, "admin");
        return ResponseEntity.ok().build();
    }

    /**
     * DELETE /{bucket} - 删除存储桶
     */
    @DeleteMapping(value = {"/{bucket}", "/{bucket}/"})
    public ResponseEntity<Void> deleteBucket(@PathVariable String bucket) {
        s3ObjectService.deleteBucket(bucket);
        return ResponseEntity.noContent().build();
    }

    /**
     * GET /{bucket} - 列出桶内对象（支持带/和不带/两种路径）
     */
    @GetMapping(value = {"/{bucket}", "/{bucket}/"})
    public ResponseEntity<String> listObjects(@PathVariable String bucket,
                                              @RequestParam(required = false) String prefix,
                                              @RequestParam(required = false) String delimiter,
                                              @RequestParam(required = false) Integer maxKeys,
                                              @RequestParam(required = false) String marker) {
        String keyPrefix = Optional.ofNullable(prefix).orElse("");
        int limit = maxKeys != null ? maxKeys : 1000;

        List<StorageBackendPort.ObjectInfo> objects = s3ObjectService.listObjects(ListObjectsRequest.builder()
                .bucketName(bucket)
                .prefix(keyPrefix)
                .delimiter(delimiter)
                .maxKeys(limit)
                .marker(marker)
                .build());

        // 检查是否被截断（返回的对象数等于请求的maxKeys）
        boolean isTruncated = objects.size() >= limit;
        // 如果被截断，设置下一页的marker为最后一个对象的key
        String nextMarker = isTruncated && !objects.isEmpty() ? objects.get(objects.size() - 1).getKey() : null;

        ListObjectsResponse response = ListObjectsResponse.builder()
                .name(bucket)
                .prefix(Optional.ofNullable(prefix).orElse(""))
                .maxKeys(limit)
                .marker(Optional.ofNullable(marker).orElse(""))
                .nextMarker(nextMarker)
                .isTruncated(isTruncated)
                .contents(objects.stream()
                        .map(obj -> ListObjectsResponse.Contents.builder()
                                .key(obj.getKey())
                                .lastModified(new Date(obj.getLastModified()))
                                .eTag("\"" + obj.getETag() + "\"")
                                .size(obj.getSize())
                                .storageClass("STANDARD")
                                .build())
                        .toList())
                .build();

        try {
            String xml = xmlMapper.writeValueAsString(response);
            return ResponseEntity.status(HttpStatus.OK)
                    .contentType(MediaType.APPLICATION_XML)
                    .body(xml);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GET /{bucket}?list-type=2 - ListObjectsV2接口
     */
    @GetMapping(value = {"/{bucket}", "/{bucket}/"}, params = "list-type=2")
    public ResponseEntity<String> listObjectsV2(@PathVariable String bucket,
                                                @RequestParam(required = false) String prefix,
                                                @RequestParam(required = false) String delimiter,
                                                @RequestParam(required = false) Integer maxKeys,
                                                @RequestParam(required = false, name = "continuation-token") String continuationToken) {
        String keyPrefix = Optional.ofNullable(prefix).orElse("");
        int limit = maxKeys != null ? maxKeys : 1000;

        List<StorageBackendPort.ObjectInfo> objects = s3ObjectService.listObjects(ListObjectsRequest.builder()
                .bucketName(bucket)
                .prefix(keyPrefix)
                .delimiter(delimiter)
                .maxKeys(limit)
                .marker(continuationToken)
                .build());

        // 检查是否被截断（返回的对象数等于请求的maxKeys）
        boolean isTruncated = objects.size() >= limit;
        // 如果被截断，设置下一页的continuationToken为最后一个对象的key
        String nextContinuationToken = isTruncated && !objects.isEmpty() ? objects.get(objects.size() - 1).getKey() : null;

        ListObjectsV2Response response = ListObjectsV2Response.builder()
                .name(bucket)
                .prefix(Optional.ofNullable(prefix).orElse(""))
                .continuationToken(Optional.ofNullable(continuationToken).orElse(""))
                .nextContinuationToken(nextContinuationToken)
                .maxKeys(limit)
                .isTruncated(isTruncated)
                .contents(objects.stream()
                        .map(obj -> ListObjectsV2Response.Contents.builder()
                                .key(obj.getKey())
                                .lastModified(new Date(obj.getLastModified()))
                                .eTag("\"" + obj.getETag() + "\"")
                                .size(obj.getSize())
                                .storageClass("STANDARD")
                                .build())
                        .toList())
                .build();

        try {
            String xml = xmlMapper.writeValueAsString(response);
            return ResponseEntity.status(HttpStatus.OK)
                    .contentType(MediaType.APPLICATION_XML)
                    .body(xml);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @RequestMapping(value = "/{bucket}/{key:.*}", method = HEAD)
    public ResponseEntity<Void> headObject(@PathVariable String bucket,
                                           @PathVariable String key) {
        boolean exists = s3ObjectService.objectExists(bucket, key);

        if (!exists) {
            return ResponseEntity.notFound().build();
        }

        Optional<S3Object> s3ObjectOpt = s3ObjectService.getObjectMetadata(bucket, key);
        if (s3ObjectOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        S3Object s3Object = s3ObjectOpt.get();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, s3Object.getContentType())
                .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(s3Object.getSize()))
                .header(HttpHeaders.ETAG, "\"" + s3Object.getETag() + "\"")
                .header(HttpHeaders.LAST_MODIFIED, s3Object.getLastModified().toString())
                .build();
    }

    /**
     * PUT /{bucket}/{key} - 上传对象
     */
    @PutMapping("/{bucket}/{key:.*}")
    public ResponseEntity<Void> putObject(@PathVariable String bucket,
                                          @PathVariable String key,
                                          @RequestHeader(value = HttpHeaders.CONTENT_TYPE, defaultValue = "application/octet-stream") String contentType,
                                          @RequestHeader(value = HttpHeaders.CONTENT_LENGTH, defaultValue = "0") long contentLength,
                                          HttpServletRequest request) throws IOException {
        validateBinaryUploadContentType(contentType, "putObject");
        try (InputStream inputStream = request.getInputStream()) {
            HashMap<String, String> metadata = new HashMap<>();
            request.getHeaderNames().asIterator().forEachRemaining(headerName -> {
                if (headerName.toLowerCase().startsWith("x-amz-meta-")) {
                    String metaKey = headerName.substring("x-amz-meta-".length());
                    metadata.put(metaKey, request.getHeader(headerName));
                }
            });

            String eTag = s3ObjectService.putObject(PutObjectRequest.builder()
                    .bucketName(bucket)
                    .key(key)
                    .content(inputStream)
                    .size(contentLength)
                    .contentType(contentType)
                    .metadata(metadata)
                    .build());

            return ResponseEntity.ok()
                    .header(HttpHeaders.ETAG, "\"" + eTag + "\"")
                    .build();
        }
    }

    /**
     * GET /{bucket}/{key} - 下载对象
     */
    @GetMapping("/{bucket}/{key:.*}")
    public ResponseEntity<Void> getObject(@PathVariable String bucket,
                                          @PathVariable String key,
                                          HttpServletResponse response) throws IOException {
        Optional<S3Object> s3ObjectOpt = s3ObjectService.getObject(bucket, key);
        if (s3ObjectOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        S3Object s3Object = s3ObjectOpt.get();

        response.setContentType(s3Object.getContentType());
        response.setContentLengthLong(s3Object.getSize());
        response.setHeader(HttpHeaders.ETAG, "\"" + s3Object.getETag() + "\"");
        response.setHeader(HttpHeaders.LAST_MODIFIED, s3Object.getLastModified().toString());
        response.setHeader(HttpHeaders.CACHE_CONTROL, "public, max-age=31536000");

        s3Object.getMetadata().forEach((k, v) -> {
            response.setHeader("x-amz-meta-%s".formatted(k), v);
        });

        try (InputStream inputStream = s3Object.getContent()) {
            inputStream.transferTo(response.getOutputStream());
            response.flushBuffer();
        }

        return ResponseEntity.ok().build();
    }

    /**
     * DELETE /{bucket}/{key} - 删除对象
     */
    @DeleteMapping("/{bucket}/{key:.*}")
    public ResponseEntity<Void> deleteObject(@PathVariable String bucket,
                                             @PathVariable String key) {
        s3ObjectService.deleteObject(bucket, key);
        return ResponseEntity.noContent().build();
    }

    /**
     * POST /{bucket}/{key}?uploads - 初始化分块上传
     */
    @PostMapping(value = "/{bucket}/{key:.*}", params = "uploads")
    public ResponseEntity<String> initiateMultipartUpload(@PathVariable String bucket,
                                                          @PathVariable String key,
                                                          @RequestHeader(value = HttpHeaders.CONTENT_TYPE, defaultValue = "application/octet-stream") String contentType) {
        String uploadId = s3ObjectService.initiateMultipartUpload(bucket, key, contentType);

        InitiateMultipartUploadResponse response = InitiateMultipartUploadResponse.builder()
                .bucket(bucket)
                .key(key)
                .uploadId(uploadId)
                .build();

        try {
            String xml = xmlMapper.writeValueAsString(response);
            return ResponseEntity.status(HttpStatus.OK)
                    .contentType(MediaType.APPLICATION_XML)
                    .body(xml);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * PUT /{bucket}/{key}?partNumber&uploadId - 上传分块
     */
    @PutMapping(value = "/{bucket}/{key:.*}", params = {"partNumber", "uploadId"})
    public ResponseEntity<Void> uploadPart(@PathVariable String bucket,
                                           @PathVariable String key,
                                           @RequestParam int partNumber,
                                           @RequestParam String uploadId,
                                           @RequestHeader(value = HttpHeaders.CONTENT_TYPE, required = false) String contentType,
                                           @RequestHeader(value = HttpHeaders.CONTENT_LENGTH, required = false) Long contentLengthHeader,
                                           HttpServletRequest request) throws IOException {
        validateBinaryUploadContentType(contentType, "uploadPart");
        try (InputStream inputStream = request.getInputStream()) {
            long contentLength = contentLengthHeader != null
                    ? contentLengthHeader
                    : request.getContentLengthLong();
            if (contentLength < 0) {
                contentLength = -1L;
            }
            String eTag = s3ObjectService.uploadPart(UploadPartRequest.builder()
                    .uploadId(uploadId)
                    .partNumber(partNumber)
                    .content(inputStream)
                    .size(contentLength)
                    .bucketName(bucket)
                    .objectKey(key)
                    .build());
            return ResponseEntity.ok()
                    .header(HttpHeaders.ETAG, "\"" + eTag + "\"")
                    .build();
        }
    }

    /**
     * POST /{bucket}/{key}?uploadId - 完成分块上传
     */
    @PostMapping(value = "/{bucket}/{key:.*}", params = "uploadId")
    public ResponseEntity<String> completeMultipartUpload(@PathVariable String bucket,
                                                          @PathVariable String key,
                                                          @RequestParam String uploadId,
                                                          @RequestBody String requestBody) {
        String eTag = s3ObjectService.completeMultipartUpload(bucket, key, uploadId, requestBody);

        String location = String.format("http://localhost:9000/%s/%s", bucket, key);
        CompleteMultipartUploadResponse response = CompleteMultipartUploadResponse.builder()
                .location(location)
                .bucket(bucket)
                .key(key)
                .eTag("\"" + eTag + "\"")
                .build();

        try {
            String xml = xmlMapper.writeValueAsString(response);
            return ResponseEntity.status(HttpStatus.OK)
                    .contentType(MediaType.APPLICATION_XML)
                    .body(xml);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * DELETE /{bucket}/{key}?uploadId - 取消分块上传
     */
    @DeleteMapping(value = "/{bucket}/{key:.*}", params = "uploadId")
    public ResponseEntity<Void> abortMultipartUpload(@PathVariable String bucket,
                                                     @PathVariable String key,
                                                     @RequestParam String uploadId) {
        s3ObjectService.abortMultipartUpload(uploadId);
        return ResponseEntity.noContent().build();
    }

    /**
     * POST /{bucket}/{key}?delete - 批量删除对象
     */
    @PostMapping(value = {"/{bucket}", "/{bucket}/"}, params = "delete")
    public ResponseEntity<String> deleteObjects(@PathVariable String bucket,
                                                @RequestBody String requestBody) {
        return doDeleteObjects(bucket, "", requestBody);
    }

    @PostMapping(value = "/{bucket}/{key:.*}", params = "delete")
    public ResponseEntity<String> deleteObjects(@PathVariable String bucket,
                                                @PathVariable String key,
                                                @RequestBody String requestBody) {
        return doDeleteObjects(bucket, key, requestBody);
    }

    private ResponseEntity<String> doDeleteObjects(String bucket, String keyPrefix, String requestBody) {
        List<String> deletedKeys = s3ObjectService.deleteObjects(bucket, keyPrefix, requestBody);
        DeleteObjectsResponse response = DeleteObjectsResponse.builder()
                .deleted(deletedKeys.stream()
                        .map(deletedKey -> DeleteObjectsResponse.Deleted.builder()
                                .key(deletedKey)
                                .build())
                        .toList())
                .build();

        try {
            String xml = xmlMapper.writeValueAsString(response);
            return ResponseEntity.status(HttpStatus.OK)
                    .contentType(MediaType.APPLICATION_XML)
                    .body(xml);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 异常处理
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException e) {
        ErrorResponse response = ErrorResponse.builder()
                .code("InvalidRequest")
                .message(e.getMessage())
                .build();

        try {
            String xml = xmlMapper.writeValueAsString(response);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .contentType(MediaType.APPLICATION_XML)
                    .body(xml);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GET /{bucket}/{key}?acl - 获取对象ACL（暂未实现）
     */
    @GetMapping(value = "/{bucket}/{key:.*}", params = "acl")
    @SuppressWarnings("all")
    public ResponseEntity<String> getObjectAcl(@PathVariable String bucket,
                                               @PathVariable String key,
                                               HttpServletRequest request) {
        throw new UnsupportedOperationException("ACL功能暂未实现");
    }

    /**
     * PUT /{bucket}/{key}?acl - 设置对象ACL（暂未实现）
     */
    @PutMapping(value = "/{bucket}/{key:.*}", params = "acl")
    @SuppressWarnings("all")
    public ResponseEntity<String> setObjectAcl(@PathVariable String bucket,
                                               @PathVariable String key,
                                               @RequestBody String requestBody,
                                               HttpServletRequest request) {
        throw new UnsupportedOperationException("ACL功能暂未实现");
    }

    private void validateBinaryUploadContentType(String contentType, String operation) {
        if (contentType == null) {
            return;
        }
        String ct = contentType.toLowerCase();
        if (ct.startsWith("application/x-www-form-urlencoded")) {
            throw new IllegalArgumentException(operation + " requires binary request body; do not use form-urlencoded content type");
        }
    }
}