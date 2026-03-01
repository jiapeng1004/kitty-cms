# 转码服务前后端改造提案

## Why

当前转码服务存在以下限制，需要改造以支持企业级场景与持久化运维：

1. **任务与策略仅存 Redis**：重启或 Redis 故障会导致任务与策略数据丢失，无法满足审计与恢复需求。
2. **策略模型过于简单**：现有策略为单一步骤的扁平参数（分辨率、码率等），无法表达多步骤流水线（如：转码 → 探测 → 多码率输出 → 抽帧 → 水印等），与真实企业转码系统的子策略表（如 `ct_subpolicies`）能力不匹配。
3. **输入输出能力不足**：仅支持本地磁盘路径输入；输出仅有本地路径，无法通过配置项生成 HTTP 访问地址，不利于与 CDN/对象存储对接。
4. **前端与后端未对齐**：后端若改为 DB 持久化与多步骤策略，前端需支持策略步骤编排、输入类型选择、输出 HTTP 前缀展示等。

因此需要起草本提案：在保留 Redisson 分布式锁与队列的前提下，引入 MyBatis Plus + 关系型存储，扩展输入/输出与策略模型，并同步调整前后端。

## What Changes

- **任务存储后端**：由“仅 Redis”改为 **MyBatis Plus + 关系型数据库**，默认 **H2 file**，可选 **MySQL**；任务表持久化，Redis/Redisson 仅用于队列与分布式锁。
- **输入扩展**：支持 **HTTP 地址** 与 **磁盘路径** 两种输入；对 HTTP 输入需支持临时下载、本地缓存与清理策略。
- **输出扩展**：输出仍为 **磁盘路径**，新增 **HTTP 地址** 通过配置项（如 `output.http.prefix`）作为前缀与相对路径拼接生成，便于对接 CDN/对象存储 URL。
- **转码策略重构**：参考企业转码子策略表（`转码策略.sql` 中的 `ct_subpolicies`）的 **步骤 + 依赖 + type + param** 设计，但 **不设独立大策略表**，采用 **单表同时表达“策略”与“子策略”**：同一策略由多行组成（如 `strategy_id + step_id`），每行即一个子步骤（type、depends、param 等）。
- **Redisson 角色**：继续使用 Redisson 实现 **分布式锁**（防重复消费、互斥写）与 **任务队列**（入队/出队）；任务主数据与策略主数据存数据库，队列中仅放任务 ID 或轻量引用。
- **前端改造**：任务创建支持选择输入类型（HTTP/磁盘）、展示输出磁盘路径与 HTTP 地址；策略管理支持按策略查看/编辑多步骤（步骤顺序、依赖、类型、param JSON）；必要时增加输出 HTTP 前缀的配置界面。

## Capabilities

### New Capabilities

- `transcode-task-persistence`: 基于 MyBatis Plus 的转码任务持久化，支持 H2 file（默认）与 MySQL，表结构包含输入/输出路径、输出 HTTP URL、策略 ID、状态、进度、错误信息、时间戳等。
- `transcode-strategy-unified-table`: 单表存储策略与子策略，参考企业 ct_subpolicies 的 step_id、depends、type、ti_anchor、param 等字段设计，支持 transcode/probe/extract_frame/concat/render/transfer 等类型及 JSON param，无独立“大策略表”。
- `transcode-input-http-and-disk`: 输入支持 HTTP URL 与磁盘路径；HTTP 输入时由文件处理模块下载到本地临时目录，转码完成后可按策略清理。
- `transcode-output-http-prefix`: 输出除磁盘路径外，通过配置项（如 `transcode.output.http-prefix`）生成 HTTP 地址（前缀 + 相对路径），并在任务结果中返回磁盘路径与 HTTP URL。
- `transcode-redisson-lock-queue`: 继续使用 Redisson 实现任务队列（RBlockingQueue 等）与分布式锁（RLock），保证多实例下任务不重复消费、关键区互斥。

### Modified Capabilities

- `transcode-strategy`: 由“单步骤扁平策略 + Redis 存储”改为“多步骤策略 + 单表 DB 存储”，策略 ID 对应一组 step 行，步骤参数与类型与参考 SQL 对齐。
- `transcode-task-management`: 任务创建/查询/取消在读写 DB 的同时，入队/出队仍走 Redisson；任务实体增加输入类型、输出 HTTP URL、可选通知与优先级等字段。
- `transcode-file-handler`: 支持 HTTP URL 解析、下载、本地缓存与清理；支持磁盘路径直接访问。
- `transcode-web-ui`: 任务表单支持输入类型（HTTP/磁盘）、输出 HTTP 展示；策略管理支持多步骤列表与 param 编辑（表单或 JSON）。

### Unchanged (Clarified)

- **Redisson**：仅用于分布式锁与队列，不作为任务/策略的持久化存储。
- **认证与 API 风格**：保持现有 AK/SK、Cookie、OpenAPI 等不变，仅扩展请求/响应字段与策略 API。

## Impact

- **依赖**：
  - 新增 MyBatis Plus、H2（默认）、MySQL 驱动（可选）；保留 Redisson、Spring Web、JavaCV/FFmpeg、现有认证与校验。
- **配置**：
  - 数据源：默认 H2 file（如 `jdbc:h2:file:./data/transcode`），可切换 MySQL。
  - 输出 HTTP：新增如 `transcode.output.http-prefix`（例如 `https://cdn.example.com/vod/`），用于拼接输出 HTTP URL。
  - 可选：HTTP 下载临时目录、过期时间、清理策略。
- **数据库表**：
  - **transcode_task**：任务表（id, 输入路径/URL, 输入类型, 策略id, 状态, 进度, 输出磁盘路径, 输出 HTTP URL, 错误信息, 优先级, 重试次数, 通知配置, 创建/开始/完成时间等）。
  - **transcode_strategy_step**：策略+子策略合一表（id, strategy_id, strategy_name, step_id, depends, type, ti_anchor, param, 创建/更新时间）；无单独“策略主表”。
  - **transcode_access_key**：AK/SK 表（id, access_key_id, secret_key, name, status, expires_at, last_used_at, 创建/更新时间, description）；Secret 仅创建时返回一次，校验时用于签名验证。
- **API**：
  - 任务创建：请求体支持 `inputType`（enum: DISK | HTTP）、`inputFile` 或 `inputUrl`（二选一或根据 inputType 校验）；响应/查询任务返回 `outputFile`、`outputHttpUrl`。
  - 策略：提供按 strategy_id 查询步骤列表、增删改步骤、整体保存策略等接口；步骤结构包含 step_id、depends、type、ti_anchor、param（JSON）。
- **前端**：
  - 任务创建：输入类型单选（磁盘/HTTP），根据类型展示路径或 URL 输入框；任务详情/列表中展示输出磁盘路径与 HTTP 地址。
  - 策略管理：策略维度展示步骤列表，支持步骤顺序、依赖、类型、param 编辑；可提供 JSON 编辑或表单化 param（如转码的 video_config、audio_config、control_options 等）。
- **兼容与迁移**：
  - 若当前生产仅用 Redis：需提供一次性或渐进式迁移方案（如将现有 Redis 中的任务/策略导入 DB）；新部署可直接使用 DB + Redisson 队列/锁。

## API 调用方式与签名防重放

- **方式一：AK/SK 登录 → Redis 会话 Token**  
  客户端用 AK/SK 调用登录接口，服务端校验通过后签发 **API Token**，Token 存入**分布式 Redis 会话**；后续请求在 Header 中携带该 Token（如 `Authorization: Bearer <token>`），无需每次签名。适用于前端、长会话。

- **方式二：AK/SK 签名（每次请求）**  
  每次请求携带公共签名参数及 **Signature**，签名逻辑参考 `openspec/specs/api-reference/签名设计.doc`：公共参数含 AccessKeyId、Format、SignatureMethod、Timestamp、SignatureVersion、**SignatureNonce**；构造规范化请求字符串（参数名排序、URL 编码）→ StringToSign = `HTTPMethod&%2F&percentEncode(CanonicalizedQueryString)` → HMAC-SHA1（key = AccessKeySecret+"&"）→ Base64 即 Signature。适用于服务间调用、无状态。

- **Nonce 单次消费**：每次签名请求必须带随机 **SignatureNonce**。服务端验签通过后，将该 Nonce 写入 Redis（如 key：`transcode:signature:nonce:{nonce}`），**TTL 设为允许的时间戳抖动时间**（如 300 秒）；同一 Nonce 再次出现则拒绝，防止重放。时间戳校验与 Nonce TTL 使用同一时间窗口配置。

## 参考：企业转码子策略表结构（转码策略.sql）

参考 `openspec/specs/api-reference/转码策略.sql` 中的 `ct_subpolicies`：

- **ct_policy_id**：策略组 ID（本方案中对应 `strategy_id`，无单独 policy 表）。
- **step_id**：步骤序号。
- **depends**：依赖步骤，如 `'[0]'`、`'[0,1,2]'`。
- **type**：步骤类型，如 `transcode`、`probe`、`extract_frame`、`concat`、`render`、`transfer`、`put_into_content_center`、`smart_render`。
- **ti_anchor**：时间锚点，如 `ti_anchor0`。
- **param**：JSON，包含 `input_config`、`output_config`、`control_options` 等（如 file_config、video_config、audio_config、watermark 等）。

本方案在单表中保留上述参数设计，去掉对独立 `ct_policies` 表的依赖，用同一张表的 `strategy_id` 聚合多行构成一个策略。

## 非目标（Out of Scope）

- 本提案不实现具体转码引擎对多步骤 DAG 的执行逻辑细节（如 probe/transfer 等），仅定义存储与 API 形态；执行引擎的逐步实现可后续迭代。
- 不改变现有认证、OpenAPI、SSE 进度等已有能力，仅扩展字段与存储。

## 实施任务

可执行任务已拆分为 **tasks.md**，按 1～10 章顺序实施即可：数据库与 MyBatis Plus → 实体与 Mapper → 任务持久化与队列 → 策略单表 → 输入输出与文件处理 → 认证与签名（登录 Token + 签名 + Nonce）→ API 扩展 → 前端改造 → 配置与运维 → 测试与文档。
