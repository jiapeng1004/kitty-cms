## ADDED Requirements

### Requirement: 编目版本必须采用 last 与 version 双字段
系统 MUST 通过 `last` 标识最新编目版本，并通过 `version` 标识历史版本序号，且版本历史 SHALL 默认永久保留。

#### Scenario: 查询最新编目
- **WHEN** 客户端请求资源最新编目
- **THEN** 系统 MUST 仅返回 `last=true` 的版本记录
