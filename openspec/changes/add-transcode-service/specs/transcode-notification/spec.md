# Transcode Notification 规范

## 1. 概述

本规范定义了转码完成通知机制，用于在转码任务完成、失败或状态变更时向相关系统或用户发送通知。通知机制是转码服务的重要组成部分，确保转码结果能够及时传递给相关方，便于后续处理流程的执行。

## 2. 功能需求

### 2.1 通知类型
- **任务完成通知**：转码任务成功完成时发送的通知
- **任务失败通知**：转码任务失败时发送的通知
- **任务取消通知**：转码任务被取消时发送的通知
- **任务进度通知**：转码任务进度更新时发送的通知
- **系统告警通知**：转码服务出现异常时发送的告警通知

### 2.2 通知方式
- **消息队列**：将通知发送到消息队列，由订阅方自行处理
- **HTTP 回调**：通过 HTTP POST 请求回调指定的接口
- **WebSocket**：通过 WebSocket 实时推送通知
- **邮件**：通过邮件发送通知（适用于重要任务）
- **短信**：通过短信发送通知（适用于紧急情况）

### 2.3 通知内容
- **任务基本信息**：任务ID、任务名称、创建时间等
- **任务状态**：当前状态、状态变更时间等
- **转码结果**：成功/失败、输出文件路径、文件大小等
- **错误信息**：失败原因、错误代码、错误详情等
- **转码统计**：转码时长、转码速度、输入/输出文件信息等
- **相关链接**：任务详情链接、输出文件访问链接等

### 2.4 通知配置
- **通知方式配置**：在转码任务发起时指定通知方式
- **通知目标配置**：在转码任务发起时指定通知目标（队列名、URL、邮箱等）
- **通知频率控制**：控制通知的发送频率，避免过多通知
- **通知重试机制**：配置通知失败时的重试策略

### 2.5 通知管理
- **通知记录**：记录所有发送的通知及其状态
- **通知查询**：支持查询通知历史记录
- **通知状态**：跟踪通知的发送状态（待发送、已发送、发送失败等）
- **通知重试**：支持重试发送失败的通知

## 3. 非功能需求

### 3.1 性能
- 通知发送响应时间：< 100ms
- 通知处理延迟：< 1s
- 支持每秒至少 100 个通知的处理能力

### 3.2 可靠性
- 通知的持久化存储
- 通知的至少一次送达保证
- 通知失败的重试机制
- 服务重启后通知的恢复发送

### 3.3 可扩展性
- 支持新增通知方式
- 支持插件式通知扩展
- 支持通知模板的自定义

### 3.4 安全性
- 通知内容的安全传输
- 通知接口的权限控制
- 防止通知内容的泄露

## 4. 数据模型

### 4.1 数据库表

#### 4.1.1 transcode_notification 表
| 字段名 | 数据类型 | 约束 | 描述 |
|-------|---------|------|------|
| id | VARCHAR(36) | PRIMARY KEY | 通知ID |
| task_id | VARCHAR(36) | NOT NULL | 任务ID |
| type | VARCHAR(20) | NOT NULL | 通知类型（COMPLETED、FAILED、CANCELLED、PROGRESS、ALARM） |
| method | VARCHAR(20) | NOT NULL | 通知方式（MQ、HTTP、WEBSOCKET、EMAIL、SMS） |
| target | VARCHAR(500) | NOT NULL | 通知目标（队列名、URL、邮箱地址等） |
| status | VARCHAR(20) | NOT NULL | 通知状态（PENDING、SENT、FAILED、RETRYING） |
| content | TEXT | NOT NULL | 通知内容 |
| retry_count | INT | NOT NULL DEFAULT 0 | 重试次数 |
| max_retries | INT | NOT NULL DEFAULT 3 | 最大重试次数 |
| created_at | DATETIME | NOT NULL | 创建时间 |
| sent_at | DATETIME | NULL | 发送时间 |
| error_message | TEXT | NULL | 错误信息 |

### 4.2 Redis 数据结构

#### 4.2.1 通知队列
- **名称**：`transcode:notification:queue`
- **类型**：Redis List 或 Redisson RBlockingQueue
- **结构**：存储待发送的通知ID

#### 4.2.2 通知锁
- **名称**：`transcode:notification:lock:{notificationId}`
- **类型**：Redis String（用于分布式锁）
- **结构**：存储锁的持有者信息

## 5. API 接口

### 5.1 通知配置接口

#### 5.1.1 配置通知方式
- **路径**：`/api/transcode/task`
- **方法**：POST
- **请求体**：
  ```json
  {
    "inputFile": "string",
    "strategyId": "string",
    "notifications": [
      {
        "method": "MQ",
        "target": "transcode.notifications"
      },
      {
        "method": "HTTP",
        "target": "http://example.com/callback"
      }
    ]
  }
  ```
- **响应**：
  ```json
  {
    "taskId": "string",
    "status": "CREATED",
    "createdAt": "timestamp"
  }
  ```

### 5.2 通知管理接口

#### 5.2.1 查询通知记录
- **路径**：`/api/transcode/notification`
- **方法**：GET
- **参数**：
  - `taskId`：任务ID（可选）
  - `type`：通知类型（可选）
  - `status`：通知状态（可选）
  - `page`：页码（默认1）
  - `size`：每页数量（默认10）
- **响应**：
  ```json
  {
    "total": 100,
    "page": 1,
    "size": 10,
    "items": [
      {
        "id": "string",
        "taskId": "string",
        "type": "COMPLETED",
        "method": "MQ",
        "target": "transcode.notifications",
        "status": "SENT",
        "createdAt": "timestamp",
        "sentAt": "timestamp"
      },
      // 更多通知记录...
    ]
  }
  ```

#### 5.2.2 重试通知
- **路径**：`/api/transcode/notification/{id}/retry`
- **方法**：POST
- **响应**：
  ```json
  {
    "notificationId": "string",
    "status": "RETRYING",
    "retryCount": 1,
    "retriedAt": "timestamp"
  }
  ```

### 5.3 通知管理接口

#### 5.3.1 取消通知
- **路径**：`/api/transcode/notification/{id}/cancel`
- **方法**：POST
- **响应**：
  ```json
  {
    "notificationId": "string",
    "status": "CANCELLED",
    "cancelledAt": "timestamp"
  }
  ```

## 6. 实现方案

### 6.1 技术选型
- **消息队列**：Redis Pub/Sub 或 Kafka
- **HTTP 客户端**：Spring WebClient
- **WebSocket**：Spring WebSocket
- **邮件发送**：Spring Mail
- **数据存储**：MySQL 数据库 + Redis 缓存
- **异步处理**：Spring WebFlux + CompletableFuture

### 6.2 核心组件
- **NotificationService**：通知服务，处理通知的发送和管理
- **NotificationSender**：通知发送器，根据不同方式发送通知
- **NotificationQueue**：通知队列，管理待发送的通知
- **NotificationRetry**：通知重试机制，处理发送失败的通知
- **NotificationMonitor**：通知监控，监控通知的发送状态

### 6.3 关键流程

#### 6.3.1 通知发送流程
1. 转码任务发起时，指定通知方式和目标
2. 任务状态变更触发通知事件
3. 根据任务配置的通知方式和目标准备通知
4. 生成通知内容
5. 创建通知记录
6. 将通知添加到发送队列
7. 通知发送器从队列中获取通知
8. 根据通知方式发送通知
9. 更新通知状态
10. 处理通知失败的情况（重试或记录）

#### 6.3.2 通知重试流程
1. 检测到通知发送失败
2. 检查重试次数是否超过限制
3. 如果未超过限制，安排重试
4. 更新通知状态为 RETRYING
5. 等待重试间隔后重新发送
6. 重复发送流程
7. 如果重试失败，更新通知状态为 FAILED

#### 6.3.3 通知配置流程
1. 接收通知配置请求
2. 验证配置参数的有效性
3. 保存配置到数据库
4. 更新 Redis 缓存
5. 应用新的配置

## 7. 监控与告警

### 7.1 监控指标
- 通知发送数量
- 通知发送成功率
- 通知发送延迟
- 通知重试次数
- 通知队列长度

### 7.2 告警机制
- 通知发送失败率过高
- 通知队列积压
- 通知服务异常

## 8. 部署与运维

### 8.1 依赖
- MySQL 8.0+
- Redis 6.0+
- JDK 25+
- Spring Boot 3.x
- 邮件服务器（可选）
- 短信服务（可选）

### 8.2 配置
- 数据库连接信息
- Redis 连接信息
- 消息队列配置
- 邮件服务器配置
- 短信服务配置
- 通知模板配置
- 重试策略配置

### 8.3 运维操作
- 通知模板管理
- 通知记录查询
- 通知服务监控
- 通知失败处理

## 9. 测试计划

### 9.1 单元测试
- 通知发送测试
- 通知模板测试
- 通知重试测试
- 通知队列测试

### 9.2 集成测试
- 与任务管理系统集成测试
- 与消息队列集成测试
- 与 HTTP 回调集成测试
- 与数据库集成测试

### 9.3 性能测试
- 并发通知发送测试
- 通知队列性能测试
- 通知模板渲染测试

### 9.4 边界测试
- 通知内容过长测试
- 通知目标不可达测试
- 网络中断测试
- 服务重启测试

## 10. 风险评估

### 10.1 潜在风险
- **通知延迟**：通知发送延迟导致相关流程延迟
- **通知丢失**：通知发送失败且重试机制失效导致通知丢失
- **通知风暴**：系统异常导致大量通知同时发送
- **外部依赖失败**：依赖的邮件服务、短信服务等外部服务失败
- **安全风险**：通知内容中包含敏感信息导致安全问题

### 10.2 缓解措施
- **异步处理**：使用异步方式发送通知，不阻塞主流程
- **多重通知方式**：配置多种通知方式，确保至少一种方式成功
- **通知限流**：实现通知限流机制，避免通知风暴
- **故障隔离**：外部服务失败时不影响核心功能
- **内容加密**：对敏感通知内容进行加密处理
- **监控告警**：实时监控通知服务状态，及时发现问题
- **降级策略**：外部服务不可用时，使用备用通知方式