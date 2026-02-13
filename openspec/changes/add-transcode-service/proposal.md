## Why

随着内容管理系统的发展，用户上传的媒体文件格式多样，需要进行统一转码以适应不同的播放场景和设备。当前系统缺乏转码能力，无法自动处理视频、音频等多媒体文件的格式转换，导致用户体验受限。引入转码服务可以自动将上传的媒体文件转换为标准格式，提高系统的兼容性和用户体验。

## What Changes

- **新增转码服务模块**：在 kitty-transcoder 模块中实现完整的转码功能
- **集成 Redis 任务队列**：引入 Redis 作为异步任务队列，实现转码任务的排队和调度
- **实现 FFmpeg 转码**：基于 JavaCV 和 FFmpeg 实现视频、音频文件的转码功能
- **多步骤转码策略**：支持复杂的多步骤转码策略，包括帧提取、转码、探测等步骤
- **灵活的输入输出配置**：支持本地共享存储文件和远程 HTTP 地址两种输入方式
- **远程文件处理**：支持 HTTP 远程文件的临时下载、缓存和释放
- **多实例并发控制**：实现多实例环境下的任务并发控制和分布式锁
- **转码前端管理界面**：基于 Vue 3 + Ant Design 的转码服务前端
- **SSE 实时进度监控**：使用 Server-Sent Events 实现转码进度的实时推送
- **转码策略管理**：支持策略的创建、修改、查看和调度
- **任务完整周期管理**：提供任务从创建到完成的完整生命周期管理
- **水印支持**：支持在转码过程中添加水印
- **变量路径支持**：支持路径中的变量替换（如 ?tenant、?year、?month、?day、?task_id）
- **AK/SK 认证机制**：实现基于 Access Key 和 Secret Key 的认证和签名机制
- **HTTP Exchange 客户端**：使用 HTTP Exchange 实现服务间调用
- **OpenAPI 文档**：使用 OpenAPI 注解自动生成 Swagger 文档
- **参数校验**：使用 Validator 注解进行参数校验

## Capabilities

### New Capabilities
- `transcode-task-queue`: 基于 Redis 的转码任务队列管理，包括任务的入队、出队、优先级调度等功能，使用 Redis 作为唯一存储
- `ffmpeg-transcode-engine`: 基于 FFmpeg 的转码引擎，支持视频和音频文件的格式转换、分辨率调整、码率控制等
- `transcode-strategy`: 转码策略管理，支持多步骤转码策略、预设策略和自定义策略，包括目标格式、分辨率、码率、编码器等配置，存储在 Redis 中
- `transcode-task-management`: 转码任务的全生命周期管理，包括任务创建、状态跟踪、进度查询、任务取消、失败重试等，所有任务状态存储在 Redis 中
- `transcode-notification`: 转码完成通知机制，在转码任务发起时配置通知方式和目标，支持多种通知渠道（消息队列、HTTP 回调、WebSocket 等）
- `transcode-file-handler`: 文件处理模块，支持本地共享存储文件和远程 HTTP 地址的输入，包括临时下载、缓存和释放
- `transcode-concurrency-control`: 多实例并发控制，使用分布式锁确保任务不会在多个实例中重复处理
- `transcode-web-ui`: 基于 Vue 3 + Ant Design 的转码服务前端管理界面，支持任务列表、进度监控、策略管理等
- `transcode-sse-progress`: 基于 Server-Sent Events 的实时进度推送，支持前端实时监控转码进度
- `transcode-watermark`: 水印支持，支持在转码过程中添加图片水印，包括位置、大小、透明度等配置
- `transcode-path-variables`: 路径变量支持，支持在输出路径中使用变量替换，如 ?tenant、?year、?month、?day、?task_id 等
- `transcode-authentication`: AK/SK 认证机制，支持 Cookie 登录和 API 签名两种方式
- `transcode-http-exchange`: HTTP Exchange 客户端，用于服务间调用
- `transcode-openapi`: OpenAPI 文档自动生成，使用注解定义接口文档
- `transcode-validation`: 参数校验，使用 Validator 注解进行参数校验

### Modified Capabilities
- 无

## Impact

- **新增依赖**：
  - Spring Boot 4.x（使用 Project Loom 虚拟线程）
  - Redis 客户端（spring-boot-starter-data-redis）
  - Redisson（用于分布式锁和任务队列）
  - JavaCV（已引入，使用其集成的 FFmpeg 库）
  - H2 数据库（默认嵌入式数据库，用于开发和测试）
  - MySQL 驱动（可选，用于生产环境）
  - Flyway 或 MyBatis-Plus 自动迁移（用于自动建表）
  - Spring Web（用于 HTTP 接口和 SSE）
  - Spring WebFlux（用于 HTTP Exchange 客户端）
  - HTTP 客户端（用于远程文件下载）
  - 自定义 AK/SK 认证（轻量级，无需 Spring Security）
  - HMAC-SHA1（用于 API 签名）
  - Springdoc OpenAPI（用于自动生成 Swagger 文档）
  - Jakarta Validation（用于参数校验）
  
- **新增模块**：
  - kitty-transcoder-api：转码服务 API 接口定义（使用 HTTP Exchange、OpenAPI 注解、Validator 注解）
  - kitty-transcoder-func：转码功能实现
  - kitty-transcoder-server：转码服务启动器
  - kitty-transcoder-web：转码服务前端管理界面（Vue 3 + Ant Design）

- **新增数据库表**：
  - transcode_task：转码任务表
  - transcode_strategy：转码策略表
  - transcode_history：转码历史记录表
  - transcode_notification：通知记录表
  - transcode_access_key：AK/SK 认证表
  
- **数据库配置**：
  - 默认使用 H2 嵌入式数据库
  - MySQL 作为可选的生产数据库
  - 支持自动迁移，无需手动 SQL 脚本
  - 严格的 schema 定义，支持自动建表

- **新增 API 接口**：
  - POST /api/transcode/task：创建转码任务
  - GET /api/transcode/task/{id}：查询转码任务
  - DELETE /api/transcode/task/{id}：取消转码任务
  - GET /api/transcode/strategy：查询转码策略列表
  - POST /api/transcode/strategy：创建转码策略
  - POST /api/auth/login：AK/SK 登录（Cookie 方式）
  - POST /api/auth/access-key：创建 Access Key
  - GET /api/auth/access-key：查询 Access Key 列表

- **系统集成**：
  - 与文件存储服务集成，获取原始文件和存储转码后的文件
  - 与 Redis 集成，实现任务队列和任务状态管理
  - 与数据库集成，实现任务持久化存储
  - 与消息队列集成，实现转码完成通知
  - HTTP Exchange 客户端支持其他服务直接引入 jar 包调用

- **认证机制**：
  - Cookie 登录方式：使用 AK/SK 进行登录，返回 Cookie
  - API 签名方式：使用 AK/SK 进行签名，所有请求包含 Client ID、Timestamp、Signature
  - 签名算法：HMAC-SHA1，参数按 ASCII 正序拼接
  - 密钥：使用 Secret Key 作为 HMAC 密钥

- **API 文档**：
  - 使用 OpenAPI 注解自动生成 Swagger 文档
  - 接口定义使用 @Tag、@Operation、@Parameter、@Schema 等注解
  - 请求和响应对象使用 @Schema 注解
  - 无需手动编写文档，通过注解自动生成
  - Swagger UI 访问地址：http://localhost:8080/swagger-ui.html

- **参数校验**：
  - 使用 Jakarta Validation 注解进行参数校验
  - 接口参数使用 @Valid、@NotNull、@NotBlank、@Size 等注解
  - 请求对象使用 Validator 注解进行字段校验
  - 自动校验并返回错误信息
  - 无需手动编写校验逻辑