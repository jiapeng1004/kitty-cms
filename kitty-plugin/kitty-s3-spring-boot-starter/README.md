# Kitty S3 兼容层 Spring Boot Starter

完全独立的Spring Boot插件式AWS S3协议兼容层，采用DDD+Port/Adapter架构设计，不侵入现有业务代码，开箱即用。

## 特性

✅ **标准S3 API兼容**：支持官方S3 SDK、S3CMD、Cyberduck等标准S3客户端直接调用  
✅ **零侵入设计**：完全独立插件，无需修改现有业务代码  
✅ **可扩展架构**：Port/Adapter端口适配器模式，支持分布式锁、存储后端、元数据存储的多种实现  
✅ **本地测试友好**：内置本地内存锁、本地文件存储、内存元数据实现，无需外部依赖  
✅ **Spring Boot自动配置**：通过application.yml配置，开箱即用  
✅ **生产级可用**：包含分布式锁、操作幂等性保证、异常处理、XML响应格式  

## 已实现API

### 存储桶操作
| API | HTTP方法 | 状态 | 说明 |
|-----|----------|------|------|
| `/` | GET | ✅ 完全实现 | 列出所有存储桶 |
| `/{bucket}` | PUT | ✅ 完全实现 | 创建存储桶 |
| `/{bucket}` | DELETE | ✅ 完全实现 | 删除存储桶 |

### 对象操作
| API | HTTP方法 | 状态 | 说明 |
|-----|----------|------|------|
| `/{bucket}/{key}` | HEAD | ✅ 完全实现 | 检查对象是否存在，返回元数据 |
| `/{bucket}/{key}` | PUT | ✅ 完全实现 | 上传对象，支持自定义元数据(x-amz-meta-*) |
| `/{bucket}/{key}` | GET | ✅ 完全实现 | 下载对象，返回Content-Type、ETag、Last-Modified、元数据 |
| `/{bucket}/{key}` | DELETE | ✅ 完全实现 | 删除对象 |

### 对象列表
| API | HTTP方法 | 状态 | 说明 |
|-----|----------|------|------|
| `/{bucket}` | GET | ✅ 完全实现 | 列出桶内对象，支持prefix、delimiter、maxKeys参数 |

### 分块上传
| API | HTTP方法 | 状态 | 说明 |
|-----|----------|------|------|
| `/{bucket}/{key}?uploads` | POST | ✅ 完全实现 | 初始化分块上传 |
| `/{bucket}/{key}?partNumber&uploadId` | PUT | ✅ 完全实现 | 上传分块 |
| `/{bucket}/{key}?uploadId` | POST | ✅ 完全实现 | 完成分块上传 |
| `/{bucket}/{key}?uploadId` | DELETE | ✅ 完全实现 | 取消分块上传 |

### 批量操作
| API | HTTP方法 | 状态 | 说明 |
|-----|----------|------|------|
| `/{bucket}/{key}?delete` | POST | ✅ 完全实现 | 批量删除对象 |

## 有限实现API

| API | HTTP方法 | 状态 | 说明 |
|-----|----------|------|------|
| `/{bucket}/{key}?acl` | GET | ❌ 未实现 | 获取对象ACL |
| `/{bucket}/{key}?acl` | PUT | ❌ 未实现 | 设置对象ACL |
| 签名验证 | - | ❌ 未实现 | AWS V2/V4签名验证 |
| 权限控制 | - | ❌ 未实现 | 基于ACL的权限控制 |

## 快速开始

### 1. 添加依赖
```xml
<dependency>
    <groupId>icu.jiapeng.kitty</groupId>
    <artifactId>kitty-s3-spring-boot-starter</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### 2. 配置
在`application.yml`中添加配置：
```yaml
kitty:
  s3:
    enabled: true                  # 是否启用S3兼容层
    port: 9000                     # 服务端口
    access-key: minioadmin         # 访问密钥
    secret-key: minioadmin         # 秘密密钥
    signature-version: V4          # 签名版本
    
    # 存储后端配置（默认本地文件存储）
    storage:
      type: local                  # 存储类型：local/minio/oss/cos
      local:
        base-path: /data/s3        # 本地存储根路径，默认临时目录
    
    # 分布式锁配置（默认本地内存锁）
    lock:
      type: local                  # 锁类型：local/redis
    
    # 元数据存储配置（默认内存存储）
    metadata:
      type: memory                 # 元数据存储类型：memory/mysql/mongodb
```

### 3. 使用

**使用标准AWS S3 SDK调用：**
```java
AmazonS3 s3Client = AmazonS3Client.builder()
    .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(
        "http://localhost:9000", "us-east-1"))
    .withCredentials(new AWSStaticCredentialsProvider(
        new BasicAWSCredentials("minioadmin", "minioadmin")))
    .withPathStyleAccessEnabled(true)
    .build();

// 创建存储桶
s3Client.createBucket("my-bucket");

// 上传对象
s3Client.putObject("my-bucket", "test.txt", "Hello World!");

// 下载对象
S3Object object = s3Client.getObject("my-bucket", "test.txt");

// 列出对象
ObjectListing listing = s3Client.listObjects("my-bucket", "prefix/");

// 分块上传
InitiateMultipartUploadRequest initRequest = new InitiateMultipartUploadRequest("my-bucket", "large-file.zip");
InitiateMultipartUploadResult initResult = s3Client.initiateMultipartUpload(initRequest);

UploadPartRequest uploadRequest = new UploadPartRequest()
    .withBucketName("my-bucket")
    .withKey("large-file.zip")
    .withUploadId(initResult.getUploadId())
    .withPartNumber(1)
    .withFileOffset(0)
    .withPartSize(partSize);
UploadPartResult partResult = s3Client.uploadPart(uploadRequest);

CompleteMultipartUploadRequest completeRequest = new CompleteMultipartUploadRequest()
    .withBucketName("my-bucket")
    .withKey("large-file.zip")
    .withUploadId(initResult.getUploadId())
    .withPartNumbers(partNumbers);
s3Client.completeMultipartUpload(completeRequest);
```

**使用curl测试：**
```bash
# 列出所有存储桶
curl http://localhost:9000/

# 创建存储桶
curl -X PUT http://localhost:9000/my-bucket

# 上传文件
curl -X PUT -T "test.txt" http://localhost:9000/my-bucket/test.txt

# 下载文件
curl http://localhost:9000/my-bucket/test.txt -o test.txt

# 检查对象是否存在
curl -I http://localhost:9000/my-bucket/test.txt

# 删除文件
curl -X DELETE http://localhost:9000/my-bucket/test.txt

# 列出对象
curl "http://localhost:9000/my-bucket?prefix=test&max-keys=100"

# 初始化分块上传
curl -X POST "http://localhost:9000/my-bucket/large-file.zip?uploads"

# 上传分块
curl -X PUT --data-binary "@part1" "http://localhost:9000/my-bucket/large-file.zip?partNumber=1&uploadId=xxx"

# 完成分块上传
curl -X POST "http://localhost:9000/my-bucket/large-file.zip?uploadId=xxx" -d @parts.xml

# 批量删除
curl -X POST "http://localhost:9000/my-bucket?delete" -d @delete.xml
```

## 扩展实现

### 自定义分布式锁实现
1. 实现`DistributedLockPort`接口
2. 使用`@ConditionalOnProperty(prefix = "kitty.s3.lock", name = "type", havingValue = "redis")`指定配置条件
3. 加入Spring上下文即可自动生效

### 自定义存储后端实现
1. 实现`StorageBackendPort`接口
2. 使用`@ConditionalOnProperty(prefix = "kitty.s3.storage", name = "type", havingValue = "minio")`指定配置条件
3. 加入Spring上下文即可自动生效

### 自定义元数据存储实现
1. 实现`MetadataStorePort`接口
2. 使用`@ConditionalOnProperty(prefix = "kitty.s3.metadata", name = "type", havingValue = "mysql")`指定配置条件
3. 加入Spring上下文即可自动生效

## 项目结构

```
kitty-s3-spring-boot-starter/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── icu/jiapeng/kitty/plugin/s3/
│   │   │       ├── port/               # 端口抽象定义
│   │   │       │   ├── DistributedLockPort.java
│   │   │       │   ├── StorageBackendPort.java
│   │   │       │   └── MetadataStorePort.java
│   │   │       ├── adapter/            # 适配器实现
│   │   │       │   └── local/          # 本地实现（测试用）
│   │   │       │       ├── LocalMemoryLockAdapter.java
│   │   │       │       ├── LocalFileSystemStorageAdapter.java
│   │   │       │       └── InMemoryMetadataStoreAdapter.java
│   │   │       ├── domain/             # 领域模型
│   │   │       │   ├── S3Object.java
│   │   │       │   └── MultipartUpload.java
│   │   │       ├── service/            # 应用服务
│   │   │       │   └── S3ObjectService.java
│   │   │       ├── controller/         # S3 API Controller
│   │   │       │   └── S3Controller.java
│   │   │       └── config/             # 自动配置
│   │   │           ├── S3Properties.java
│   │   │           └── S3AutoConfiguration.java
│   │   └── resources/
│   │       └── META-INF/
│   │           └── spring/
│   │               └── org.springframework.boot.autoconfigure.AutoConfiguration.imports
│   └── test/                           # 测试代码
│       ├── java/
│       │   └── icu/jiapeng/kitty/plugin/s3/test/
│       │       └── S3LocalTestApplication.java
│       └── resources/
│           └── application.yml
└── pom.xml
```

## 许可证
Apache License 2.0