package icu.jiapeng.kitty.plugin.s3.service;

import icu.jiapeng.kitty.plugin.s3.domain.S3Object;
import icu.jiapeng.kitty.plugin.s3.model.dto.ListObjectsRequest;
import icu.jiapeng.kitty.plugin.s3.model.dto.PutObjectRequest;
import icu.jiapeng.kitty.plugin.s3.model.dto.UploadPartRequest;
import icu.jiapeng.kitty.plugin.s3.model.dto.S3ObjectInfo;

import java.util.List;
import java.util.Optional;

/**
 * S3 协议编排服务接口。
 *
 * <p>职责边界：
 * <ul>
 *   <li>本接口定义 S3 兼容层的协议命令与查询语义（桶、对象、分片上传、批量删除等）。</li>
 *   <li>实现类负责参数校验、流程编排、元数据一致性。</li>
 *   <li>底层流读写、远程代理调用、对象合并等由存储端口实现，不在接口层约束具体技术细节。</li>
 * </ul>
 *
 * <p>异常约定：
 * <ul>
 *   <li>参数非法、资源不存在、协议体不合法等场景，建议抛出 {@link IllegalArgumentException}。</li>
 *   <li>底层存储故障可抛出运行时异常，由上层统一异常映射处理。</li>
 * </ul>
 *
 * <p>典型分片上传流程示例：
 * <pre>{@code
 * String uploadId = s3ObjectService.initiateMultipartUpload("my-bucket", "video.mp4", "video/mp4");
 *
 * s3ObjectService.uploadPart(UploadPartRequest.builder()
 *         .uploadId(uploadId)
 *         .partNumber(1)
 *         .content(part1InputStream)
 *         .size(part1Size)
 *         .bucketName("my-bucket")
 *         .objectKey("video.mp4")
 *         .build());
 *
 * s3ObjectService.uploadPart(UploadPartRequest.builder()
 *         .uploadId(uploadId)
 *         .partNumber(2)
 *         .content(part2InputStream)
 *         .size(part2Size)
 *         .bucketName("my-bucket")
 *         .objectKey("video.mp4")
 *         .build());
 *
 * String completeXml = """
 * <CompleteMultipartUpload>
 *   <Part><PartNumber>1</PartNumber><ETag>"etag-1"</ETag></Part>
 *   <Part><PartNumber>2</PartNumber><ETag>"etag-2"</ETag></Part>
 * </CompleteMultipartUpload>
 * """;
 * String finalEtag = s3ObjectService.completeMultipartUpload("my-bucket", "video.mp4", uploadId, completeXml);
 * }</pre>
 */
public interface S3ObjectService {

    /**
     * 列出所有桶名。
     *
     * @return 桶名列表；不存在时返回空列表
     */
    List<String> listBuckets();

    /**
     * 创建桶。
     *
     * @param bucketName 桶名
     * @param owner      桶拥有者标识
     * @throws IllegalArgumentException 当桶已存在或参数非法
     */
    void createBucket(String bucketName, String owner);

    /**
     * 删除桶。
     *
     * @param bucketName 桶名
     * @throws IllegalArgumentException 当桶不存在或不允许删除
     */
    void deleteBucket(String bucketName);

    /**
     * 检查对象是否存在。
     *
     * @param bucketName 桶名
     * @param key        对象键
     * @return true 存在；false 不存在
     */
    boolean objectExists(String bucketName, String key);

    /**
     * 获取对象元数据（不含对象内容流）。
     *
     * @param bucketName 桶名
     * @param key        对象键
     * @return 对象元数据；不存在返回 empty
     */
    Optional<S3Object> getObjectMetadata(String bucketName, String key);

    /**
     * 上传对象并持久化元数据。
     *
     * <p>示例：
     * <pre>{@code
     * String eTag = s3ObjectService.putObject(PutObjectRequest.builder()
     *         .bucketName("my-bucket")
     *         .key("docs/readme.txt")
     *         .content(inputStream)
     *         .size(contentLength)
     *         .contentType("text/plain")
     *         .metadata(Map.of("author", "kitty"))
     *         .build());
     * }</pre>
     *
     * @param request 上传请求 DTO
     * @return 对象 ETag
     * @throws IllegalArgumentException 当桶不存在或请求非法
     */
    String putObject(PutObjectRequest request);

    /**
     * 获取对象（元数据 + 懒打开内容流的 {@link icu.jiapeng.kitty.plugin.s3.domain.S3Object#getContentStreamSupplier()}）。
     * 不在此层打开存储流；调用方通过 {@link icu.jiapeng.kitty.plugin.s3.domain.S3Object#openContentStream()} 获取流并负责关闭。
     *
     * @param bucketName 桶名
     * @param key        对象键
     * @return 对象；不存在返回 empty
     */
    Optional<S3Object> getObject(String bucketName, String key);

    /**
     * 删除单个对象（幂等）。
     *
     * @param bucketName 桶名
     * @param key        对象键
     */
    void deleteObject(String bucketName, String key);

    /**
     * 批量删除对象。
     *
     * <p>请求体使用 S3 Delete XML 协议（如：{@code <Delete><Object><Key>...</Key></Object></Delete>}）。
     *
     * @param bucketName  桶名
     * @param keyPrefix   键前缀（可为空）
     * @param requestBody 删除 XML 请求体
     * @return 实际删除成功的对象键列表
     * @throws IllegalArgumentException 当请求体格式非法
     */
    List<String> deleteObjects(String bucketName, String keyPrefix, String requestBody);

    /**
     * 初始化分片上传并返回 uploadId。
     *
     * @param bucketName  桶名
     * @param key         最终对象键
     * @param contentType 内容类型
     * @return uploadId
     */
    String initiateMultipartUpload(String bucketName, String key, String contentType);

    /**
     * 上传单个分片。
     *
     * @param request 分片上传请求
     * @return 分片 ETag
     * @throws IllegalArgumentException 当 uploadId 无效或请求非法
     */
    String uploadPart(UploadPartRequest request);

    /**
     * 完成分片上传。
     *
     * <p>requestBody 使用 S3 CompleteMultipartUpload XML 协议，建议携带 PartNumber + ETag
     * 以保证顺序和一致性校验。
     *
     * @param bucketName  与 initiate 时路径中的桶名一致
     * @param key         与 initiate 时路径中的对象键一致
     * @param uploadId    上传会话 ID
     * @param requestBody 完成上传 XML 请求体
     * @return 最终对象 ETag
     * @throws IllegalArgumentException 当 uploadId 无效、桶/键与会话不一致、分片缺失或 ETag 不匹配
     */
    String completeMultipartUpload(String bucketName, String key, String uploadId, String requestBody);

    /**
     * 取消分片上传并清理临时分片。
     *
     * @param uploadId 上传会话 ID
     */
    void abortMultipartUpload(String uploadId);

    /**
     * 列出对象。
     *
     * @param request 列表查询请求（前缀、分页、marker 等）
     * @return 对象信息列表
     */
    List<S3ObjectInfo> listObjects(ListObjectsRequest request);
}
