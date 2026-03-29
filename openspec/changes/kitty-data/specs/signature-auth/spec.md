## ADDED Requirements

### Requirement: 签名鉴权中间件

系统 SHALL 实现基于 HMAC-SHA256 的签名鉴权中间件，验证请求的合法性。

#### Scenario: 签名验证通过
- **WHEN** 请求头包含有效的 X-AK、X-SIGN、X-TIMESTAMP，且签名正确
- **THEN** 请求通过鉴权，继续处理

#### Scenario: 缺少请求头
- **WHEN** 请求头缺少 X-AK、X-SIGN 或 X-TIMESTAMP 任意一个
- **THEN** 返回 401 Unauthorized，提示缺少必要参数

#### Scenario: AK 不存在
- **WHEN** 请求头 X-AK 不在 AK/SK 缓存中
- **THEN** 返回 401 Unauthorized，提示 AK 不存在

#### Scenario: 签名错误
- **WHEN** X-SIGN 计算结果与请求头中的 X-SIGN 不匹配
- **THEN** 返回 401 Unauthorized，提示签名错误

#### Scenario: 时间戳过期
- **WHEN** X-TIMESTAMP 与服务器时间差超过配置的容忍范围
- **THEN** 返回 401 Unauthorized，提示时间戳已过期

---

### Requirement: 签名计算方式

系统 SHALL 定义签名的计算方式为：HMAC-SHA256(secretKey, ak + timestamp)

#### Scenario: 正确计算签名
- **WHEN** 使用 SK 作为密钥，对 "AK + TIMESTAMP" 字符串进行 HMAC-SHA256 加密
- **THEN** 生成 64 位十六进制字符串作为签名
