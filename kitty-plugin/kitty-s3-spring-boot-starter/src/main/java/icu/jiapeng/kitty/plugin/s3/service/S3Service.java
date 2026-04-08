package icu.jiapeng.kitty.plugin.s3.service;

import icu.jiapeng.kitty.plugin.s3.domain.MultipartUpload;
import icu.jiapeng.kitty.plugin.s3.domain.S3Object;
import icu.jiapeng.kitty.plugin.s3.model.dto.ListObjectsRequest;
import icu.jiapeng.kitty.plugin.s3.model.dto.MultipartComposeResult;
import icu.jiapeng.kitty.plugin.s3.model.dto.MultipartPartRecord;
import icu.jiapeng.kitty.plugin.s3.model.dto.PutObjectResult;
import icu.jiapeng.kitty.plugin.s3.model.dto.S3ObjectInfo;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;

/**
 * 单一后端契约：桶/对象元数据与对象体、分片上传相关 I/O 由同一实现提供。
 * 本地开发/测试使用 {@link icu.jiapeng.kitty.plugin.s3.local.LocalS3Service}。
 */
public interface S3Service {

    List<String> listBuckets();

    void saveBucket(String bucketName, String owner);

    boolean bucketExists(String bucketName);

    void deleteBucket(String bucketName);

    void saveObjectMetadata(S3Object object);

    Optional<S3Object> getObjectMetadata(String bucket, String key);

    void deleteObjectMetadata(String bucket, String key);

    void saveMultipartUpload(MultipartUpload upload);

    Optional<MultipartUpload> getMultipartUpload(String uploadId);

    void deleteMultipartUpload(String uploadId);

    PutObjectResult putObject(String bucket, String key, InputStream content, long size);

    InputStream getObject(String bucket, String key);

    void deleteObject(String bucket, String key);

    boolean exists(String bucket, String key);

    List<S3ObjectInfo> listObjects(ListObjectsRequest request);

    MultipartComposeResult composeMultipartParts(String bucket, String destinationKey, List<String> partKeysInOrder);

    String multipartPartObjectKey(String uploadId, int partNumber);

    List<MultipartPartRecord> listMultipartParts(String bucket, String uploadId);
}
