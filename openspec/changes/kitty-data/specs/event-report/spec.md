## ADDED Requirements

### Requirement: 事件上报接口

系统 SHALL 提供事件上报接口，记录业务事件。

**返回格式**：标准 RESTful，HTTP 状态码表示结果，无通用 DTO 包装。

#### Scenario: 成功上报事件
- **WHEN** POST /api/v1/events 传入有效的事件数据
- **THEN** 事件存储到 MongoDB，返回 HTTP 200 OK，无包装返回值

#### Scenario: 事件类型为空
- **WHEN** 上报事件时 eventType 字段为空
- **THEN** 返回 400 Bad Request，提示事件类型不能为空

#### Scenario: 操作者为空
- **WHEN** 上报事件时 operator 字段为空
- **THEN** 返回 400 Bad Request，提示操作者不能为空

#### Scenario: 操作值为空
- **WHEN** 上报事件时 value 字段为空
- **THEN** 返回 400 Bad Request，提示操作值不能为空

---

### Requirement: 事件数据模型

系统 SHALL 定义事件数据模型，包含以下字段：

- **id**: 事件唯一标识（UUID）
- **eventType**: 事件类型
- **operator**: 操作者标识
- **value**: 操作值（整数，用于用量统计）
- **timestamp**: 事件发生时间
- **createTime**: 记录创建时间

#### Scenario: 普通操作事件
- **WHEN** 上报普通操作事件（如用户登录、文件上传）
- **THEN** value 值为 1

#### Scenario: 用量操作事件
- **WHEN** 上报用量操作事件（如流量用量、API 调用次数）
- **THEN** value 值为实际用量值（如 1024、-12）
