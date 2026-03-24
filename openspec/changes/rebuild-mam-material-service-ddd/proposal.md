## Why

现有 material 服务仅有基础包结构，缺少可支撑 MAM 业务的完整前后端能力，且历史 `material-func` 业务实现不满足当前 DDD、分布式、异构存储与可扩展策略要求。当前需要先完成高质量规范化提案，统一边界与约束后再进入实现阶段，降低后续返工与跨模块耦合风险。

## What Changes

- **BREAKING**: 按经典 DDD 重建 material 业务模型与分层边界，废弃并迁移 `material-func` 既有业务实现，不再兼容其领域建模方式。
- 建立 MAM 资源中心核心领域：栏目、资源、编目模板/字段/实例、栏目权限、资源任务进度、转码策略与任务、站内信、通用审核工作流。
- 统一 API 风格为标准 REST：成功返回无外层泛型包装的干净 DTO；异常统一返回专用异常 DTO；`exchange` 层定义请求/响应对象以支持 RPC 风格复用。
- 建立分布式运行基线：分布式锁、分布式队列、缓存抽象、配置中心抽象、用户上下文适配抽象，默认 infra 可用实现包括 Redisson 与 kitty-user/kitty-transcoder 的 gRPC 插件。
- 建立异构存储体系：抽象文件存储接口 + 存储工厂，支持 S3 协议与本地磁盘协议，覆盖分片上传并发控制、文件记录与存储记录解耦、多存储实例路由。
- 强化资源与栏目模型：支持无限层级栏目树（含个人栏目虚拟根/占位机制）、资源文件夹嵌套、固定 `parentId=0` 语义、栏目权限缓存与强制 public true/false 语义。
- 引入编目与版本策略：编目 EAV、模板绑定独立表、`last + version` 双字段版本化，默认永久保留版本历史。
- 引入检索与智能扩展：事务后投递队列同步 ES（全量 doc + 字段 update 双策略），并支持向量化多来源并存（含 `NONE` 安全空实现）。
- 引入任务体系：资源关联任务（转码/AI 审核/AI 标签）统一轻量任务模型，任务列表永久保留；转码失败重试保持记录主键不变，仅更新外部任务标识并逻辑删除历史态。
- 增强上传与协作能力：支持目录结构保真的文件夹上传；站内信模块使用 SSE + 队列跨 Pod 协调；通用审核工作流支持提交、审核与结果查询。
- 明确注解与污染边界：MyBatis Plus 注解仅存在 repo 实体；validation/swagger 注解仅在 api/application 层 DTO/Controller；领域聚合根保持纯净。
- 全链路国际化与错误处理：统一使用 `messageSource`，并对齐现有全局异常处理风格。

## Capabilities

### New Capabilities

- `material-ddd-architecture`: 定义 material 服务的 DDD 分层、模块边界、聚合与反腐接口约束，确保 repo/api 注解不向领域层泄漏。
- `material-rest-exchange-contract`: 定义 material 的 REST 与 exchange 契约（成功干净 DTO、异常 DTO、输入输出模型统一）。
- `material-catalog-tree`: 定义栏目树、虚拟根、个人栏目占位与跨私有根移动约束，以及栏目排序规则。
- `material-catalog-permission`: 定义栏目权限模型（角色/`public` true/false）、权限缓存与不可绕过策略。
- `material-resource-model`: 定义资源主模型、资源类型、文件夹嵌套、`parentId=0` 语义、栏目必填与关联关系。
- `material-metadata-modeling`: 定义编目模板、模板字段、模板绑定、EAV 编目实例与有序绑定关系。
- `material-metadata-versioning`: 定义 `last + version` 编目版本策略、最新版本查询与历史版本查询语义。
- `material-storage-abstraction`: 定义异构存储抽象、存储记录模型、多存储工厂路由与链接构建所需元数据。
- `material-chunk-upload`: 定义分片上传协议（`Content-Length`/`Content-Range` 头约束）、并发一致性与合并语义。
- `material-s3-compatible-starter`: 定义面向外部轻量接入的 S3 协议适配能力（Spring Boot Starter 形态）。
- `material-user-context-adapter`: 定义用户上下文/权限校验适配接口与默认 sa-token + kitty-user gRPC 实现边界。
- `material-config-center-adapter`: 定义配置中心抽象与默认 kitty-user gRPC 配置读取能力。
- `material-transcode-strategy`: 定义转码平台标识、策略参数、栏目绑定优先规则与未来流程引擎扩展点。
- `material-resource-task-center`: 定义资源关联任务中心模型（转码/AI 审核/AI 标签）与永久任务列表视图。
- `material-transcode-retry-policy`: 定义转码失败重试语义（逻辑删除历史记录、保持业务记录 id 稳定）。
- `material-search-sync`: 定义 DB 主、ES 边缘的同步机制（事务后队列投递，支持 doc 全量与字段增量两类消息）。
- `material-vector-indexing`: 定义资源向量化多来源并存模型（MySQL + ES8 向量字段）与 `NONE` 安全实现约束。
- `material-review-workflow`: 定义通用审核工作流（提交、审批、结果查询）的领域契约。
- `material-internal-message-sse`: 定义站内信模型与 SSE 实时推送、跨 Pod 队列协调机制。
- `material-folder-upload`: 定义文件夹上传时目录结构保真与资源落位规则。

### Modified Capabilities

- 无（当前 `openspec/specs` 下无既有能力规范，全部以新能力建立）

## Impact

- 受影响代码范围：`kitty-func`（material 相关前后端）、`kitty-plugin`（S3/用户/转码 gRPC 适配）、`kitty-common`（异常 DTO、国际化、队列/锁抽象）、`kitty-grpc`（必要协议扩展）。
- 受影响 API：material 相关全部接口契约；需补齐/扩展 `kitty-user-func` 权限与用户上下文 gRPC 能力（若现有不足）。
- 受影响基础设施：MySQL、ES8、Redisson、SSE 网关链路、分布式队列（默认 Redisson，可扩展 Kafka）。
- 受影响数据模型：栏目、资源、编目、权限、存储、任务、向量、站内信、审核流程等核心表结构与索引策略。
- 兼容性影响：对历史 `material-func` 业务模型与接口行为存在破坏性调整，需要以迁移方案和灰度策略配套落地。
