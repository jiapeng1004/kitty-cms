# 转码服务改造设计

## 1. 存储架构

- **任务**：MyBatis Plus + 关系型 DB（默认 H2 file，可选 MySQL）；表 `transcode_task`。
- **策略**：同一数据库，单表 `transcode_strategy_step` 存储“策略 + 子策略”，无独立策略主表。
- **队列与锁**：Redisson 任务队列（如 `transcode:task:queue` 存任务 ID）、Redisson 分布式锁（消费与写库时使用）；不存任务/策略主数据。

## 2. 单表策略设计（策略与子策略合一）

参考 `转码策略.sql` 中的 `ct_subpolicies`，用一张表同时表示“策略”和“子策略”：

- 一个**策略** = 同一 `strategy_id` 下的多行步骤。
- 每行 = 一个**子策略（步骤）**，包含步骤序号、依赖、类型、锚点、JSON 参数。

### 2.1 表：transcode_strategy_step

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | BIGINT / VARCHAR(36) | PK, 自增或 UUID | 主键 |
| strategy_id | VARCHAR(64) | NOT NULL, 索引 | 策略逻辑 ID，同 ID 多行组成一个策略 |
| strategy_name | VARCHAR(200) | NULL | 策略展示名，可仅在第一行或 step_id=0 填写 |
| step_id | INT UNSIGNED | NOT NULL | 步骤序号，同一 strategy_id 内唯一 |
| depends | VARCHAR(256) | NULL | 依赖步骤，如 `[0]`、`[0,1,2]`，JSON 数组字符串 |
| type | VARCHAR(64) | NOT NULL | 步骤类型：transcode, probe, extract_frame, concat, render, transfer, put_into_content_center, smart_render 等 |
| ti_anchor | VARCHAR(64) | NULL | 时间锚点，如 ti_anchor0 |
| param | TEXT / JSON | NULL | JSON，含 input_config、output_config、control_options 等（与参考 SQL 一致） |
| created_at | DATETIME | NOT NULL | 创建时间 |
| updated_at | DATETIME | NULL | 更新时间 |

**唯一约束**：`(strategy_id, step_id)` 唯一，便于按策略 + 步骤查询与更新。

**与参考 SQL 的对应**：

- 原 `ct_policy_id` → `strategy_id`（不再有 ct_policies 表）。
- 原 `step_id`、`depends`、`type`、`ti_anchor`、`param` 直接保留语义。
- `strategy_name` 为本方案新增，便于列表与前端展示；可选仅在 step_id=0 一行存储。

### 2.2 param JSON 结构（与参考一致）

- **transcode**：`input_config`（group_type, media_type）、`output_config`（file_config、video_config、audio_config）、可选 `control_options`（如 watermark、use_cpu 等）。
- **probe**：`input_configurations` 数组。
- **extract_frame**：`input_config`、`output_config`、`control_options`（extract_type、frame_count、frame_numbers 等）。
- **concat / render / transfer / put_into_content_center / smart_render**：按参考 SQL 中的 param 结构保留为 JSON。

不在此设计内对 param 做强 schema 校验，由实现层与前端按类型解析。

## 3. 任务表设计：transcode_task

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | VARCHAR(36) | PK | 任务 ID |
| input_type | VARCHAR(16) | NOT NULL | 输入类型：DISK / HTTP |
| input_path | VARCHAR(1024) | NOT NULL | 输入：磁盘路径或 HTTP URL |
| strategy_id | VARCHAR(64) | NOT NULL, 索引 | 使用的策略 ID（对应 strategy_step.strategy_id） |
| status | VARCHAR(32) | NOT NULL | PENDING, PROCESSING, COMPLETED, FAILED, CANCELLED |
| progress | INT | NOT NULL DEFAULT 0 | 0–100 |
| output_path | VARCHAR(1024) | NULL | 输出磁盘路径（多输出时可主路径或 JSON） |
| output_http_url | VARCHAR(1024) | NULL | 输出 HTTP 地址（由配置前缀 + 相对路径生成） |
| error_message | TEXT | NULL | 失败原因 |
| priority | INT | NOT NULL DEFAULT 5 | 优先级 |
| created_at | DATETIME | NOT NULL | 创建时间 |
| started_at | DATETIME | NULL | 开始时间 |
| completed_at | DATETIME | NULL | 完成时间 |

可选扩展：retry_count、notification_config（JSON）等，与现有 add-transcode-service 设计对齐即可。

## 4. 输入与输出行为

### 4.1 输入

- **DISK**：`input_path` 为本地或共享盘路径，直接用于转码引擎。
- **HTTP**：`input_path` 为 URL，文件处理模块先下载到本地临时目录，将本地路径交给转码引擎；任务结束后可按策略删除临时文件。

### 4.2 输出

- **磁盘路径**：由引擎与策略 param 的 `output_config.file_config.save_path` 等决定，写入 `transcode_task.output_path`。
- **HTTP 地址**：通过配置项（如 `transcode.output.http-prefix`）与相对路径拼接得到，写入 `transcode_task.output_http_url`；相对路径可由 output_path 与本地根路径换算，或由策略/引擎直接产出。

## 5. Redisson 使用方式

- **队列**：任务创建时写 DB 后，将 `task.id` 放入 Redisson RBlockingQueue（如 `transcode:task:queue`）；消费者取到 taskId 后从 DB 加载任务再执行。
- **分布式锁**：同一任务执行时对 `taskId` 或资源键加锁，防止多实例重复执行；写任务状态/进度时可按需加短锁。

## 6. 前端改造要点

- **任务创建**：选择输入类型（磁盘/HTTP），根据类型展示“路径”或“URL”输入框；提交后展示输出磁盘路径与 HTTP URL（若已配置前缀）。
- **策略管理**：按 `strategy_id` 展示策略步骤列表；支持步骤的增删改（step_id、depends、type、ti_anchor、param）；param 支持 JSON 编辑或按 type 的表单化编辑（如 transcode 的 video_config、audio_config、watermark）。
- **配置**：可选管理界面配置“输出 HTTP 前缀”，对应 `transcode.output.http-prefix`。

## 7. 与参考 SQL 的差异小结

- **不设 ct_policies 表**：策略的“主信息”仅通过 `strategy_id` + 可选 `strategy_name` 在 `transcode_strategy_step` 中表达。
- **单表**：所有步骤都在 `transcode_strategy_step`，通过 `strategy_id` 聚合为一套策略。
- **param 与 type**：与 `转码策略.sql` 中 ct_subpolicies 的 type/param 设计保持一致，便于后续对接或迁移企业已有策略数据。

---

## 8. 完整表结构（DDL 风格）

### 8.1 转码任务表 transcode_task

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | VARCHAR(36) | PK | 任务 ID |
| input_type | VARCHAR(16) | NOT NULL | 输入类型：DISK / HTTP |
| input_path | VARCHAR(1024) | NOT NULL | 输入：磁盘路径或 HTTP URL |
| strategy_id | VARCHAR(64) | NOT NULL, 索引 | 使用的策略 ID |
| status | VARCHAR(32) | NOT NULL, 默认 'PENDING' | PENDING, PROCESSING, COMPLETED, FAILED, CANCELLED |
| progress | INT | NOT NULL DEFAULT 0 | 0–100 |
| output_path | VARCHAR(1024) | NULL | 输出磁盘路径（多输出时可主路径或 JSON） |
| output_http_url | VARCHAR(1024) | NULL | 输出 HTTP 地址 |
| error_message | TEXT | NULL | 失败原因 |
| priority | INT | NOT NULL DEFAULT 5 | 优先级 1–10 |
| retry_count | INT | NOT NULL DEFAULT 0 | 已重试次数 |
| notification_config | TEXT | NULL | 通知配置 JSON（可选） |
| created_at | DATETIME | NOT NULL | 创建时间 |
| started_at | DATETIME | NULL | 开始时间 |
| completed_at | DATETIME | NULL | 完成时间 |
| created_by_ak | VARCHAR(64) | NULL | 创建任务使用的 AccessKeyId（审计用） |

**索引建议**：`strategy_id`、`status`、`created_at`；按需加 `(status, created_at)` 组合索引。

### 8.2 策略步骤表 transcode_strategy_step（策略+子策略合一）

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | BIGINT | PK, 自增 | 主键 |
| strategy_id | VARCHAR(64) | NOT NULL, 索引 | 策略逻辑 ID |
| strategy_name | VARCHAR(200) | NULL | 策略展示名（可仅 step_id=0 存） |
| step_id | INT UNSIGNED | NOT NULL | 步骤序号 |
| depends | VARCHAR(256) | NULL | 依赖步骤，如 [0]、[0,1,2] |
| type | VARCHAR(64) | NOT NULL | transcode, probe, extract_frame, concat, render, transfer 等 |
| ti_anchor | VARCHAR(64) | NULL | 时间锚点 |
| param | TEXT | NULL | JSON 参数 |
| created_at | DATETIME | NOT NULL | 创建时间 |
| updated_at | DATETIME | NULL | 更新时间 |

**唯一约束**：`UNIQUE(strategy_id, step_id)`。

### 8.3 AK/SK 表 transcode_access_key

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | BIGINT | PK, 自增 | 主键 |
| access_key_id | VARCHAR(64) | NOT NULL, UNIQUE | 授权密钥 ID（即 AccessKeyId，对外） |
| secret_key | VARCHAR(128) | NOT NULL | Secret Key（存储时建议加密或哈希后存） |
| name | VARCHAR(100) | NULL | 密钥备注名 |
| status | VARCHAR(20) | NOT NULL DEFAULT 'ACTIVE' | ACTIVE, DISABLED, EXPIRED |
| expires_at | DATETIME | NULL | 过期时间 |
| last_used_at | DATETIME | NULL | 最后使用时间 |
| created_at | DATETIME | NOT NULL | 创建时间 |
| updated_at | DATETIME | NULL | 更新时间 |
| description | VARCHAR(500) | NULL | 描述 |

说明：Secret Key 仅在创建时返回一次，后续不展示；校验时用 AccessKeyId 查表取 Secret Key 参与签名验证。

---

## 9. API 调用方式（两种）

### 9.1 方式一：AK/SK 登录 → Redis 会话 Token

- 客户端使用 AK/SK 调用**登录接口**（如 `POST /api/auth/login`），服务端校验 AK/SK 后生成 **API Token**（如 UUID），将 Token 与 AccessKeyId、过期时间等写入 **Redis**（分布式会话缓存）。
- 后续请求在 Header 中携带该 Token（如 `Authorization: Bearer <token>` 或 `X-Api-Token: <token>`），服务端从 Redis 解析会话，无需每次签名。
- 适用场景：前端 Web、长时间会话、同一客户端连续调用。

**Redis 会话结构示例**：

- Key：`transcode:session:token:{token}`
- Value（Hash）：`accessKeyId`、`createdAt`、`expiresAt`
- TTL：与会话有效期一致（如 24 小时）

### 9.2 方式二：AK/SK 签名（每次请求带签名）

- 每次请求在 URL 或 Header 中携带**公共签名参数** + **Signature**，服务端按《签名设计.doc》规则验签。
- **必须包含随机 Nonce**；Nonce 使用后**单次消费**：验签通过后将该 Nonce 写入 Redis，若同一 Nonce 再次出现则拒绝（防重放）。
- 适用场景：服务间调用、无状态、高安全要求。

**签名与 Nonce 规则**（见下节）。

---

## 10. 签名设计与 Nonce 防重放（参考 签名设计.doc）

### 10.1 公共签名参数（GET 或参与签名的参数）

与 `openspec/specs/api-reference/签名设计.doc` 一致，转码服务采用**服务授权**模型时可使用：

| 参数 | 类型 | 说明 |
|------|------|------|
| AccessKeyId | varchar | 授权密钥 ID |
| Format | varchar | 固定 JSON |
| SignatureMethod | varchar | 固定 HMAC-SHA1 |
| Timestamp | int | 请求时间戳（秒） |
| SignatureVersion | varchar | 固定 1.0 |
| SignatureNonce | varchar | **唯一随机数，防重放，每次请求必须不同** |
| Signature | - | 不参与构造签名的参数，为计算得到的签名值 |

若需租户维度可增加 GroupId；POST 请求时，参与签名的参数可包含公共参数 + 业务参数（按约定排序后参与 Canonicalized Query String）。

### 10.2 签名计算步骤

1. **构造规范化请求字符串 Canonicalized Query String**  
   参与签名的参数 = 公共参数（除 Signature） + 业务 GET/约定业务参数；按参数名**字典序排序**；对参数名和参数值做 **UTF-8 URL 编码**（RFC 3986，空格为 %20）；用 `=` 连接名与值，用 `&` 连接各对，得到规范化字符串。

2. **构造被签名字符串 StringToSign**  
   `StringToSign = HTTPMethod + "&" + percentEncode("/") + "&" + percentEncode(CanonicalizedQueryString)`  
   即：`GET&%2F&` + 对规范化字符串再次 URL 编码后的结果。

3. **计算 HMAC**  
   使用 **HMAC-SHA1**，key = `AccessKeySecret + "&"`（Secret 为服务端根据 AccessKeyId 查表所得），对 StringToSign 计算 HMAC。

4. **得到 Signature**  
   对 HMAC 二进制结果做 **Base64** 编码，即 Signature；作为参数随请求提交时需再次 URL 编码。

### 10.3 Nonce 单次消费与 Redis 存储

- **每次签名请求必须带随机 SignatureNonce**；服务端验签通过后，将 Nonce 视为**已使用**。
- **单次消费**：验签成功后，将 `SignatureNonce` 写入 Redis（例如 key：`transcode:signature:nonce:{nonce}`，value 可为 1 或 AccessKeyId），并设置 **TTL = 允许的时间戳抖动时间**（如 300 秒、900 秒）。  
  - 同一 Nonce 再次请求时，Redis 已存在该 key，直接拒绝（防重放）。  
  - 过期后 key 自动删除，无需额外清理；时间戳校验时需同时满足 \|server_time - Timestamp\| ≤ 允许抖动，避免旧请求滥用。
- **建议**：允许时间戳抖动与 Nonce TTL 使用同一配置项（如 `auth.signature.timestamp-drift-seconds`），典型值 300。

---

## 11. Redis 键汇总（认证与防重放）

| 键 | 类型 | TTL | 说明 |
|----|------|-----|------|
| transcode:session:token:{token} | Hash | 会话有效期 | 登录后的 API Token 会话 |
| transcode:signature:nonce:{nonce} | String | 时间戳抖动时间（如 300s） | 已使用的签名 Nonce，单次消费 |
| transcode:task:queue | Queue | - | 任务 ID 队列（Redisson） |
| （其他业务键按需） | - | - | 如任务状态缓存等 |
