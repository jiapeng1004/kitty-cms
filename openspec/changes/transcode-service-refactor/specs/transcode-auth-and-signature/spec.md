# 转码服务认证与签名规范

## 1. 概述

转码 API 支持两种调用方式：**AK/SK 登录 + Redis 会话 Token**、**AK/SK 每次请求签名**。签名算法与公共参数参考 `openspec/specs/api-reference/签名设计.doc`；每次签名请求必须带随机 Nonce，Nonce 单次消费并存入 Redis，TTL 为允许的时间戳抖动时间。

## 2. 方式一：登录 Token（Redis 会话）

### 2.1 登录接口

- **路径**：`POST /api/auth/login`
- **请求体**：`{ "accessKeyId": "xxx", "secretKey": "xxx" }`（或 accessKey / secretKey，与现有命名一致即可）
- **成功响应**：返回 API Token（如 UUID），并可选 Set-Cookie；Token 与 accessKeyId、过期时间写入 Redis。
- **Redis 键**：`transcode:session:token:{token}`，类型 Hash，字段如 `accessKeyId`、`createdAt`、`expiresAt`；TTL = 会话有效期（如 24 小时）。

### 2.2 携带 Token 访问 API

- 后续请求在 Header 中携带：`Authorization: Bearer <token>` 或 `X-Api-Token: <token>`。
- 服务端根据 Token 从 Redis 取会话，校验未过期即视为已认证；无需再验签。

## 3. 方式二：请求签名（参考 签名设计.doc）

### 3.1 公共签名参数（必须参与签名，且随请求携带）

| 参数名 | 类型 | 说明 |
|--------|------|------|
| AccessKeyId | string | 授权密钥 ID |
| Format | string | 固定 JSON |
| SignatureMethod | string | 固定 HMAC-SHA1 |
| Timestamp | int | 请求时间戳（**秒**） |
| SignatureVersion | string | 固定 1.0 |
| SignatureNonce | string | **唯一随机数，每次请求必须不同，防重放** |
| Signature | string | 不参与构造签名，为计算得到的签名值 |

若为 GET，业务 GET 参数也参与签名；若为 POST，按约定（如 body 不参与或 body 摘要放入某参数）扩展。

### 3.2 签名计算步骤

1. **参与签名的参数** = 公共参数（除 Signature） + 业务参数（若有）；按**参数名字典序**排序。
2. **Canonicalized Query String**：对每个参数名、参数值做 **UTF-8 URL 编码**（RFC 3986，空格 %20）；`encoded(key)=encoded(value)` 用 `&` 连接。
3. **StringToSign** = `HTTPMethod + "&" + percentEncode("/") + "&" + percentEncode(CanonicalizedQueryString)`  
   即：`GET&%2F&` + 对上述规范化字符串再次 URL 编码的结果。
4. **HMAC**：算法 HMAC-SHA1，key = `AccessKeySecret + "&"`（Secret 由服务端按 AccessKeyId 查表得到）。
5. **Signature** = 对 HMAC 二进制结果做 **Base64** 编码；作为请求参数提交时需再次 URL 编码。

### 3.3 时间戳校验

- 服务端收到请求后，校验 `|当前时间(秒) - Timestamp| ≤ 允许抖动时间`（如 300 秒），超出则拒绝。

### 3.4 Nonce 单次消费（防重放）

- 验签通过且时间戳通过后，检查 Redis 是否存在 key：`transcode:signature:nonce:{SignatureNonce}`。
- **若已存在**：拒绝请求（视为重放）。
- **若不存在**：写入 Redis，key = `transcode:signature:nonce:{SignatureNonce}`，value 可为 1 或 accessKeyId；**TTL = 允许的时间戳抖动时间**（与时间戳校验使用同一配置，如 300 秒）。
- 同一 Nonce 在 TTL 内再次出现必被拒绝；过期后 key 自动删除。

## 4. 配置项建议

| 配置项 | 说明 | 示例 |
|--------|------|------|
| auth.session.ttl-seconds | 登录 Token 会话 TTL | 86400（24 小时） |
| auth.signature.timestamp-drift-seconds | 时间戳允许抖动 / Nonce Redis TTL | 300 |

## 5. AK/SK 表（transcode_access_key）

见主设计文档 `design.md` 第 8.3 节：access_key_id、secret_key、status、expires_at 等；Secret 仅创建时返回一次，校验时仅用于服务端验签。

## 6. Redis 键汇总（认证相关）

| 键 | 类型 | TTL | 说明 |
|----|------|-----|------|
| transcode:session:token:{token} | Hash | 会话 TTL | 登录后的 API Token 会话 |
| transcode:signature:nonce:{nonce} | String | 时间戳抖动时间 | 已使用的签名 Nonce，单次消费 |
