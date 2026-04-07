## Why

当前系统缺乏标准的S3协议兼容层，无法直接对接各类S3客户端、第三方存储服务和生态工具。通过开发独立的Spring Boot插件式S3兼容层，无需修改现有业务代码即可快速扩展对象存储能力，统一存储访问接口，降低与云厂商的耦合度，同时支持本地测试和分布式部署场景。

## What Changes

- ✅ **新增独立S3兼容层Spring Boot Starter插件**，完全独立，不侵入现有业务代码
- ✅ **基于标准Spring Web Controller实现AWS S3 API子集**，支持常用操作（对象上传、下载、删除、列表、元数据管理等）
- ✅ **DDD架构设计**，通过Port端口接口抽象分布式锁、文件存储、元数据存储等依赖
- ✅ **分布式锁适配层**，内置Redis、本地内存等多种锁实现，可扩展支持其他分布式锁实现
- ✅ **存储后端适配层**，内置本地文件系统、MinIO、阿里云OSS等实现，可扩展支持其他存储
- ✅ **Test范围提供本地内存+本地文件系统实现**，支持单元测试和本地开发调试
- ✅ **生产级可用MVP实现**，包含签名验证、权限控制、限流、监控等必备能力

## Capabilities

### New Capabilities
- `s3-api-compatibility`: AWS S3 API 标准兼容层，实现常用S3接口规范
- `distributed-lock-port`: 分布式锁抽象端口，支持多种锁实现适配
- `storage-backend-port`: 存储后端抽象端口，支持多种存储系统适配
- `s3-spring-boot-starter`: S3兼容层Spring Boot Starter插件，开箱即用
- `s3-local-impl`: S3兼容层本地实现，用于测试和本地开发场景

### Modified Capabilities
无，本插件完全独立，不修改现有能力

## Impact

- 无侵入式设计，不影响现有业务代码和实现
- 新增Maven依赖：`spring-boot-starter-web`、`aws-java-sdk-s3`（仅用于API模型）、`redisson`（可选，Redis分布式锁实现）
- 新增独立部署包，端口独立，不占用现有服务端口
- 支持独立部署或集成部署两种模式
- 完全兼容现有文件存储系统，可作为现有存储的S3协议网关