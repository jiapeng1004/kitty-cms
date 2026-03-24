## ADDED Requirements

### Requirement: 分片上传必须使用 Header 协议并保证并发一致性
系统 MUST 使用 `Content-Length` 与 `Content-Range` 请求头表达分片信息，并在分布式部署下通过幂等与锁机制保证分片并发写入一致性。

#### Scenario: 并发提交相同分片
- **WHEN** 两个请求同时提交同一上传会话的同一分片范围
- **THEN** 系统 SHALL 仅接受一个有效写入并保证最终分片状态无重复或覆盖错误
