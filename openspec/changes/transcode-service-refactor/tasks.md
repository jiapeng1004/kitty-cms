# 转码服务改造任务清单

本文档将提案拆分为可执行任务，实现顺序建议按章节顺序进行。

---

## 1. 数据库与 MyBatis Plus 基础

- [ ] 1.1 在 kitty-transcoder 模块引入 MyBatis Plus、H2、MySQL 驱动依赖
- [ ] 1.2 配置数据源：默认 H2 file（如 `jdbc:h2:file:./data/transcode`），支持 profile 切换 MySQL
- [ ] 1.3 编写建表脚本或 Flyway/Liquibase 迁移：transcode_task、transcode_strategy_step、transcode_access_key（与 design.md 第 8 节一致）
- [ ] 1.4 配置 MyBatis Plus 扫描与 BaseMapper
- [ ] 1.5 验证 H2 与 MySQL 下建表与启动正常

---

## 2. 实体与 Mapper

- [ ] 2.1 实现 TranscodeTask 实体及 TranscodeTaskMapper（对应 transcode_task 表）
- [ ] 2.2 实现 TranscodeStrategyStep 实体及 TranscodeStrategyStepMapper（对应 transcode_strategy_step 表）
- [ ] 2.3 实现 TranscodeAccessKey 实体及 TranscodeAccessKeyMapper（对应 transcode_access_key 表）
- [ ] 2.4 为任务表、策略步骤表、AK 表补充必要索引（strategy_id、status、created_at 等）

---

## 3. 任务持久化与队列（Redisson 仅队列+锁）

- [ ] 3.1 改造 TaskService：创建任务时先写入 DB（transcode_task），再将 taskId 入队 Redisson RBlockingQueue
- [ ] 3.2 改造 TaskService：getTask/cancelTask/updateProgress 等改为读写在 DB，必要时配合 Redisson 锁
- [ ] 3.3 改造 TaskQueueProcessor：从队列取出 taskId 后从 DB 加载任务再执行，执行中更新 DB 状态与 output_path、output_http_url
- [ ] 3.4 任务执行时对 taskId 或资源键加 Redisson 分布式锁，防止多实例重复执行
- [ ] 3.5 实现输出 HTTP URL 生成逻辑：根据配置项 transcode.output.http-prefix 与输出相对路径拼接，写入 output_http_url

---

## 4. 策略单表（策略+子策略合一）

- [ ] 4.1 实现 StrategyStepService：按 strategy_id 查询步骤列表、按 (strategy_id, step_id) 增删改单步
- [ ] 4.2 策略列表：按 strategy_id 分组返回策略及其步骤（或仅返回 strategy_id + strategy_name 列表，详情按需查步骤）
- [ ] 4.3 创建/更新策略：支持批量写入或更新 transcode_strategy_step 行，保证 (strategy_id, step_id) 唯一
- [ ] 4.4 删除策略：按 strategy_id 删除所有步骤行
- [ ] 4.5 转码引擎或任务执行层从 DB 按 strategy_id 加载步骤列表，按 depends/step_id 排序后执行（执行逻辑可先占位或仅支持单步 transcode）

---

## 5. 输入输出与文件处理

- [ ] 5.1 任务创建 API：请求体支持 inputType（DISK/HTTP）、input_path（磁盘路径或 HTTP URL），校验与落库
- [ ] 5.2 实现 HTTP 输入处理：下载到本地临时目录，将本地路径交给转码引擎；任务结束后可选清理临时文件
- [ ] 5.3 实现 DISK 输入：直接使用 input_path 作为本地路径
- [ ] 5.4 配置项：transcode.output.http-prefix、HTTP 下载临时目录与清理策略（可选）

---

## 6. 认证与签名（两种方式 + Nonce 防重放）

- [ ] 6.1 实现登录接口 POST /api/auth/login：校验 AK/SK（查 transcode_access_key），通过后生成 API Token，写入 Redis transcode:session:token:{token}，TTL 为会话有效期
- [ ] 6.2 实现 Token 校验过滤器/拦截器：从 Header Authorization Bearer 或 X-Api-Token 取 Token，从 Redis 取会话，有效则放行
- [ ] 6.3 实现签名校验：按《签名设计.doc》解析公共参数（AccessKeyId、Timestamp、SignatureNonce、Signature 等），构造 Canonicalized Query String 与 StringToSign，用 AccessKeySecret+"&" 做 HMAC-SHA1，Base64 与请求 Signature 比对
- [ ] 6.4 实现时间戳校验：|当前时间 - Timestamp| ≤ auth.signature.timestamp-drift-seconds（如 300）
- [ ] 6.5 实现 Nonce 单次消费：验签通过后检查 Redis transcode:signature:nonce:{nonce} 不存在则写入并设置 TTL = timestamp-drift-seconds；已存在则拒绝
- [ ] 6.6 认证入口：请求优先识别 Token，无 Token 再走签名校验；二者通过其一即可
- [ ] 6.7 实现 Access Key 管理 API：创建（返回 access_key_id + secret_key 仅一次）、列表、禁用/删除；Secret 存储时可选加密或哈希
- [ ] 6.8 配置项：auth.session.ttl-seconds、auth.signature.timestamp-drift-seconds

---

## 7. API 扩展

- [ ] 7.1 任务创建/查询/取消 API：请求与响应与 design.md 一致，包含 inputType、input_path、output_path、output_http_url、strategy_id 等
- [ ] 7.2 策略 API：按 strategy_id 查询步骤列表、创建/更新策略（批量步骤）、删除策略
- [ ] 7.3 进度与 SSE：在任务状态与进度写入 DB 的前提下，保持或扩展现有进度查询与 SSE 推送
- [ ] 7.4 所有需认证的接口支持两种方式：Token 或 签名

---

## 8. 前端改造

- [ ] 8.1 任务创建页：输入类型选择（DISK/HTTP），根据类型展示路径或 URL 输入框；策略选择（下拉或列表）；提交后展示任务 id
- [ ] 8.2 任务列表/详情：展示 output_path、output_http_url（若存在）、input_type、input_path、status、progress
- [ ] 8.3 策略管理：按 strategy_id 展示策略列表，进入策略后展示步骤列表（step_id、depends、type、ti_anchor、param）；支持步骤增删改、param 的 JSON 或表单编辑
- [ ] 8.4 登录页：支持 AK/SK 登录，登录成功后保存 Token，后续请求携带 Token
- [ ] 8.5 可选：配置页或说明中展示输出 HTTP 前缀的配置方式（transcode.output.http-prefix）
- [ ] 8.6 Access Key 管理页：创建密钥（展示一次 secret）、列表、禁用/删除

---

## 9. 配置与运维

- [ ] 9.1 application.yml 中增加数据源、transcode.output.http-prefix、auth.session.ttl-seconds、auth.signature.timestamp-drift-seconds、HTTP 下载临时目录等配置项及文档注释
- [ ] 9.2 健康检查：DB 连接、Redis 连接、队列长度（可选）
- [ ] 9.3 若现有环境有 Redis 中的任务/策略数据：提供迁移脚本或说明，将数据导入 DB（可选）

---

## 10. 测试与文档

- [ ] 10.1 单元测试：StrategyStepService、TaskService（含 DB 与队列联动）、签名生成与校验、Nonce 单次消费
- [ ] 10.2 集成测试：登录 → Token 访问任务 API；签名方式访问任务 API；Nonce 重放被拒绝
- [ ] 10.3 更新 OpenAPI/Swagger 描述：任务与策略请求/响应字段、认证方式说明（Bearer Token + 签名参数）
- [ ] 10.4 在 README 或运维文档中说明：两种 API 调用方式、签名算法参考、Nonce 防重放、配置项含义

---

完成上述任务后，转码服务将具备：任务与策略 DB 持久化、Redisson 仅作队列与锁、输入 HTTP/磁盘、输出路径与 HTTP 前缀、策略单表多步骤、AK/SK 登录 Token 与签名双通道认证及 Nonce 防重放。
