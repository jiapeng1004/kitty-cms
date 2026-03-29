## ADDED Requirements

### Requirement: SSE 实时监控接口

系统 SHALL 提供 SSE（Server-Sent Events）接口，用于实时监控指定事件和操作者。

#### Scenario: 建立 SSE 连接
- **WHEN** 客户端发起 GET /api/v1/events/monitor?eventType=xxx&operator=xxx
- **THEN** 建立 SSE 连接，持续推送匹配条件的新事件

#### Scenario: 监控所有操作者
- **WHEN** GET /api/v1/events/monitor?eventType=xxx（不指定 operator）
- **THEN** 推送该事件类型的所有新事件

#### Scenario: 监控所有事件类型
- **WHEN** GET /api/v1/events/monitor?operator=xxx（不指定 eventType）
- **THEN** 推送该操作者的所有新事件

#### Scenario: 监控指定事件和操作者
- **WHEN** GET /api/v1/events/monitor?eventType=xxx&operator=xxx
- **THEN** 仅推送同时匹配事件类型和操作者的新事件

#### Scenario: 连接断开
- **WHEN** 客户端断开 SSE 连接
- **THEN** 服务器端关闭该连接，释放资源

---

### Requirement: SSE 消息格式

系统 SHALL 定义 SSE 消息格式，包含事件数据。

#### Scenario: 推送事件消息
- **WHEN** 有新事件匹配监控条件
- **THEN** 推送 JSON 格式的事件数据，event 字段为 "event"
