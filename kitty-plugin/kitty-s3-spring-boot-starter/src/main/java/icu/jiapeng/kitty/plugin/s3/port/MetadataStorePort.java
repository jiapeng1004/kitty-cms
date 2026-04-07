package icu.jiapeng.kitty.plugin.s3.port;

import icu.jiapeng.kitty.plugin.s3.domain.S3Object;
import icu.jiapeng.kitty.plugin.s3.domain.MultipartUpload;

import java.util.List;
import java.util.Optional;

/**
 * 元数据存储端口抽象
 * 适配不同元数据存储实现
 */
public interface MetadataStorePort {

    /**
     * 获取所有存储桶列表
     * @return 桶名列表
     */
    List<String> listBuckets();

    /**
     * 保存桶元数据
     *
     * @param bucketName 桶名
     * @param owner      所属者
     */
    void saveBucket(String bucketName, String owner);

    /**
     * 检查桶是否存在
     *
     * @param bucketName 桶名
     * @return 是否存在
     */
    boolean bucketExists(String bucketName);

    /**
     * 删除桶
     *
     * @param bucketName 桶名
     */
    void deleteBucket(String bucketName);

    /**
     * 保存对象元数据
     *
     * @param object 对象元数据
     */
    void saveObjectMetadata(S3Object object);

    /**
     * 获取对象元数据
     *
     * @param bucket 桶名
     * @param key    对象键
     * @return 对象元数据
     */
    Optional<S3Object> getObjectMetadata(String bucket, String key);

    /**
     * 删除对象元数据
     *
     * @param bucket 桶名
     * @param key    对象键
     */
    void deleteObjectMetadata(String bucket, String key);

    /**
     * 保存分块上传信息
     *
     * @param upload 分块上传信息
     */
    void saveMultipartUpload(MultipartUpload upload);

    /**
     * 获取分块上传信息
     *
     * @param uploadId 上传ID
     * @return 分块上传信息
     */
    Optional<MultipartUpload> getMultipartUpload(String uploadId);

    /**
     * 删除分块上传信息
     *
     * @param uploadId 上传ID
     */
    void deleteMultipartUpload(String uploadId);
}