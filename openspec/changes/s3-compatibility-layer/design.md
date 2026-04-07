## 架构概览

本S3兼容层采用**DDD领域驱动设计**+**Port/Adapter端口适配器架构**，完全独立插件化设计，不侵入现有业务代码，通过Spring Boot Starter自动配置实现开箱即用。

整体分层架构：
```
┌───────────────────────────────────────────────────────────┐
│                      接口层 (Interface Layer)            │
│  - S3标准Controller：实现AWS S3 REST API规范              │
│  - 请求/响应转换：S3 XML/JSON格式处理，签名验证           │
└───────────────────────────────────────────────────────────┘
                              ↓
┌───────────────────────────────────────────────────────────┐
│                      应用层 (Application Layer)          │
│  - S3应用服务：协调领域服务和端口调用                     │
│  - 事务控制、权限校验、限流熔断                           │
└───────────────────────────────────────────────────────────┘
                              ↓
┌───────────────────────────────────────────────────────────┐
│                      领域层 (Domain Layer)                │
│  - 核心领域模型：Bucket、S3Object、MultipartUpload        │
│  - 领域服务：对象操作、分块上传、元数据管理               │
│  - 端口定义（Port）：分布式锁、存储后端、元数据存储抽象   │
└───────────────────────────────────────────────────────────┘
                              ↓
┌───────────────────────────────────────────────────────────┐
│                  基础设施层 (Infrastructure Layer)        │
│  - 适配器实现（Adapter）：各个Port的具体实现               │
│    - 分布式锁适配器：Redis、本地内存、ZooKeeper等          │
│    - 存储后端适配器：本地文件、MinIO、OSS、COS等           │
│    - 元数据存储适配器：MySQL、MongoDB、内存等              │
└───────────────────────────────────────────────────────────┘
```

## 核心领域模型

### 1. Bucket (存储桶)
```java
public class Bucket {
    private String name;           // 桶名，全局唯一
    private String owner;          // 所属者
    private LocalDateTime createTime; // 创建时间
    private Map<String, String> metadata; // 自定义元数据
    private AccessControlList acl; // 访问控制列表
}
```

### 2. S3Object (对象)
```java
public class S3Object {
    private String bucketName;     // 所属桶名
    private String key;            // 对象键
    private long size;             // 文件大小
    private String contentType;    // MIME类型
    private String eTag;           // 文件哈希
    private LocalDateTime lastModified; // 最后修改时间
    private Map<String, String> metadata; // 自定义元数据
    private InputStream content;    // 对象内容流
}
```

### 3. MultipartUpload (分块上传)
```java
public class MultipartUpload {
    private String uploadId;       // 上传ID
    private String bucketName;     // 桶名
    private String key;            // 对象键
    private LocalDateTime createTime; // 创建时间
    private Map<Integer, Part> parts; // 已上传分块
}
```

## Port端口抽象设计

所有外部依赖通过Port接口抽象，实现可插拔、易扩展的特性。

### 1. 分布式锁端口 `DistributedLockPort`
```java
public interface DistributedLockPort {
    /**
     * 尝试获取锁
     * @param key 锁Key
     * @param expireSeconds 过期时间（秒）
     * @return 是否获取成功
     */
    boolean tryLock(String key, long expireSeconds);

    /**
     * 释放锁
     * @param key 锁Key
     */
    void unlock(String key);

    /**
     * 执行锁保护的操作
     * @param key 锁Key
     * @param expireSeconds 过期时间
     * @param action 要执行的操作
     * @return 操作结果
     */
    <T> T executeWithLock(String key, long expireSeconds, Supplier<T> action);
}
```

### 2. 存储后端端口 `StorageBackendPort`
```java
public interface StorageBackendPort {
    /**
     * 存储对象
     * @param bucket 桶名
     * @param key 对象键
     * @param content 对象内容
     * @param size 对象大小
     * @return ETag哈希值
     */
    String putObject(String bucket, String key, InputStream content, long size);

    /**
     * 获取对象
     * @param bucket 桶名
     * @param key 对象键
     * @return 对象内容
     */
    InputStream getObject(String bucket, String key);

    /**
     * 删除对象
     * @param bucket 桶名
     * @param key 对象键
     */
    void deleteObject(String bucket, String key);

    /**
     * 检查对象是否存在
     * @param bucket 桶名
     * @param key 对象键
     * @return 是否存在
     */
    boolean exists(String bucket, String key);

    /**
     * 列出对象
     * @param bucket 桶名
     * @param prefix 前缀
     * @param delimiter 分隔符
     * @param maxKeys 最大返回数
     * @return 对象列表
     */
    List<ObjectInfo> listObjects(String bucket, String prefix, String delimiter, int maxKeys);
}
```

### 3. 元数据存储端口 `MetadataStorePort`
```java
public interface MetadataStorePort {
    /**
     * 保存对象元数据
     * @param object 对象元数据
     */
    void saveObjectMetadata(S3Object object);

    /**
     * 获取对象元数据
     * @param bucket 桶名
     * @param key 对象键
     * @return 对象元数据
     */
    Optional<S3Object> getObjectMetadata(String bucket, String key);

    /**
     * 删除对象元数据
     * @param bucket 桶名
     * @param key 对象键
     */
    void deleteObjectMetadata(String bucket, String key);

    /**
     * 保存分块上传信息
     * @param upload 分块上传信息
     */
    void saveMultipartUpload(MultipartUpload upload);

    /**
     * 获取分块上传信息
     * @param uploadId 上传ID
     * @return 分块上传信息
     */
    Optional<MultipartUpload> getMultipartUpload(String uploadId);
}
```

## Adapter适配器实现

### 1. 分布式锁实现
- **本地内存锁实现**：`LocalMemoryLockAdapter`，基于Guava Cache实现，用于单节点部署和测试场景
- **Redis锁实现**：`RedisLockAdapter`，基于Redisson实现，用于分布式部署场景
- **扩展支持**：可快速适配ZooKeeper、Etcd等其他分布式锁实现

### 2. 存储后端实现
- **本地文件系统实现**：`LocalFileSystemStorageAdapter`，本地磁盘存储，用于测试和本地开发
- **MinIO实现**：`MinioStorageAdapter`，对接MinIO对象存储
- **阿里云OSS实现**：`OssStorageAdapter`，对接阿里云OSS
- **腾讯云COS实现**：`CosStorageAdapter`，对接腾讯云COS
- **扩展支持**：可快速适配其他S3兼容存储服务

### 3. 元数据存储实现
- **内存实现**：`InMemoryMetadataStoreAdapter`，基于ConcurrentHashMap实现，用于测试
- **MySQL实现**：`MysqlMetadataStoreAdapter`，关系型数据库存储
- **MongoDB实现**：`MongodbMetadataStoreAdapter`，文档型数据库存储

## S3 API设计

基于标准AWS S3 REST API规范，实现核心常用接口：

| API路径 | HTTP方法 | 功能描述 |
|---------|----------|----------|
| `/` | GET | 列出所有存储桶 |
| `/{bucket}` | PUT | 创建存储桶 |
| `/{bucket}` | DELETE | 删除存储桶 |
| `/{bucket}` | GET | 列出桶内对象 |
| `/{bucket}/{key}` | PUT | 上传对象 |
| `/{bucket}/{key}` | GET | 下载对象 |
| `/{bucket}/{key}` | DELETE | 删除对象 |
| `/{bucket}/{key}?uploadId` | POST | 初始化分块上传 |
| `/{bucket}/{key}?partNumber&uploadId` | PUT | 上传分块 |
| `/{bucket}/{key}?uploadId` | POST | 完成分块上传 |
| `/{bucket}/{key}?uploadId` | DELETE | 取消分块上传 |
| `/{bucket}/{key}?acl` | GET | 获取对象ACL |
| `/{bucket}/{key}?acl` | PUT | 设置对象ACL |

所有API严格遵循AWS S3请求/响应格式，支持官方SDK、S3CMD、Cyberduck等标准S3客户端直接调用。

## Spring Boot插件化设计

### 1. 自动配置
通过`@AutoConfiguration`实现自动装配，配置项通过`application.yml`自定义：
```yaml
s3:
  enabled: true                  # 是否启用S3兼容层
  port: 9000                     # 服务端口
  access-key: minioadmin         # 默认AK
  secret-key: minioadmin         # 默认SK
  signature-version: V4          # 签名版本
  storage:
    type: local                  # 存储类型：local/minio/oss/cos
    local:
      base-path: /data/s3        # 本地存储路径
  lock:
    type: local                  # 锁类型：local/redis
  metadata:
    type: memory                 # 元数据存储类型：memory/mysql/mongodb
```

### 2. 零侵入设计
- 完全独立的Maven模块，与业务代码无耦合
- 通过Spring Boot自动配置加载，无需修改现有代码
- 支持独立部署（单独Jar包运行）或集成部署（作为依赖加入现有服务）两种模式

## 本地测试实现

在Test Scope提供完整的本地实现，支持单元测试和本地开发：
1. **内存元数据存储**：基于HashMap，无需外部数据库
2. **临时文件存储**：基于临时目录，测试完成自动清理
3. **本地内存锁**：单节点锁实现，无需Redis等中间件
4. **测试工具类**：提供S3客户端测试工具，方便集成测试

## 非功能设计

### 安全性
- AWS V2/V4签名验证，支持自定义签名算法
- 基于ACL的权限控制，支持公共读、私有读写等权限
- 细粒度的操作权限控制
- 支持HTTPS传输加密

### 性能
- 零拷贝文件传输，支持大文件流式上传下载
- 分块上传断点续传，最大支持5TB单文件
- 元数据缓存，减少数据库访问
- 连接池复用，提升并发能力

### 可扩展性
- Port/Adapter架构，方便扩展新的锁、存储、元数据实现
- 拦截器机制，支持自定义前置/后置处理
- 事件通知机制，支持操作审计、数据同步等扩展
- 监控指标暴露，支持Prometheus采集

### 可靠性
- 操作幂等性保证，重复请求不产生副作用
- 分布式锁防止并发写入冲突
- 元数据与存储数据一致性保证
- 异常回滚机制，失败操作自动清理临时数据