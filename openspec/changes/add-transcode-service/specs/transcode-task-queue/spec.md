# Transcode Task Queue 规范

## 1. 概述

本规范定义了基于 Redis 的转码任务队列管理系统，用于处理转码任务的入队、出队、优先级调度等功能。系统使用 Redis 作为唯一存储，避免数据库依赖，保持轻量级设计。

## 2. 功能需求

### 2.1 任务入队
- 支持将转码任务添加到队列
- 支持设置任务优先级
- 支持批量入队操作
- 入队时生成唯一任务ID

### 2.2 任务出队
- 支持从队列中获取任务（FIFO顺序）
- 支持基于优先级的任务获取
- 支持阻塞式和非阻塞式获取
- 出队时标记任务为处理中状态

### 2.3 任务管理
- 支持任务取消
- 支持任务重试
- 支持任务优先级调整
- 支持任务状态查询

### 2.4 队列管理
- 支持获取队列长度
- 支持清空队列
- 支持队列监控
- 支持队列优先级管理

## 3. 非功能需求

### 3.1 性能
- 入队操作响应时间：< 10ms
- 出队操作响应时间：< 10ms
- 支持每秒至少 1000 个任务的处理能力

### 3.2 可靠性
- 任务持久化，避免服务重启后任务丢失
- 支持任务状态的原子更新
- 提供任务处理的幂等性保证

### 3.3 可扩展性
- 支持水平扩展转码服务实例
- 支持分布式部署
- 支持动态调整队列大小

### 3.4 安全性
- 任务数据的安全存储
- 避免任务数据泄露
- 支持任务权限控制

## 4. 数据模型

### 4.1 数据库表

#### 4.1.1 transcode_task 表
| 字段名 | 数据类型 | 约束 | 描述 |
|-------|---------|------|------|
| id | VARCHAR(36) | PRIMARY KEY | 任务ID |
| status | VARCHAR(20) | NOT NULL | 任务状态（PENDING、PROCESSING、COMPLETED、FAILED、CANCELLED） |
| progress | INT | NOT NULL DEFAULT 0 | 转码进度（0-100） |
| input_file | VARCHAR(500) | NOT NULL | 输入文件路径 |
| output_file | VARCHAR(500) | NOT NULL | 输出文件路径 |
| strategy_id | VARCHAR(36) | NOT NULL | 转码策略ID |
| priority | INT | NOT NULL DEFAULT 5 | 任务优先级（1-10） |
| created_at | DATETIME | NOT NULL | 创建时间 |
| started_at | DATETIME | NULL | 开始时间 |
| completed_at | DATETIME | NULL | 完成时间 |
| error_message | TEXT | NULL | 错误信息 |
| retry_count | INT | NOT NULL DEFAULT 0 | 重试次数 |

#### 4.1.2 transcode_strategy 表
| 字段名 | 数据类型 | 约束 | 描述 |
|-------|---------|------|------|
| id | VARCHAR(36) | PRIMARY KEY | 策略ID |
| name | VARCHAR(100) | NOT NULL | 策略名称 |
| target_format | VARCHAR(50) | NOT NULL | 目标格式 |
| resolution | VARCHAR(20) | NOT NULL | 目标分辨率 |
| bitrate | VARCHAR(20) | NOT NULL | 目标码率 |
| encoder | VARCHAR(50) | NOT NULL | 编码器 |
| preset | VARCHAR(50) | NOT NULL | 预设配置 |
| video_codec | VARCHAR(50) | NOT NULL | 视频编码器 |
| audio_codec | VARCHAR(50) | NOT NULL | 音频编码器 |
| created_at | DATETIME | NOT NULL | 创建时间 |
| updated_at | DATETIME | NOT NULL | 更新时间 |

#### 4.1.3 transcode_history 表
| 字段名 | 数据类型 | 约束 | 描述 |
|-------|---------|------|------|
| id | VARCHAR(36) | PRIMARY KEY | 历史记录ID |
| task_id | VARCHAR(36) | NOT NULL | 任务ID |
| input_file | VARCHAR(500) | NOT NULL | 输入文件路径 |
| output_file | VARCHAR(500) | NOT NULL | 输出文件路径 |
| strategy_id | VARCHAR(36) | NOT NULL | 转码策略ID |
| status | VARCHAR(20) | NOT NULL | 任务状态 |
| progress | INT | NOT NULL | 转码进度 |
| error_message | TEXT | NULL | 错误信息 |
| created_at | DATETIME | NOT NULL | 创建时间 |
| started_at | DATETIME | NULL | 开始时间 |
| completed_at | DATETIME | NULL | 完成时间 |

### 4.2 Redis 数据结构

#### 4.2.1 任务队列
- **名称**：`transcode:queue`
- **类型**：Redis List 或 Redisson RBlockingQueue
- **结构**：存储任务ID列表
- **操作**：LPUSH/RPOP、BLPOP 等

#### 4.2.2 任务实时状态
- **名称**：`transcode:task:{taskId}`
- **类型**：Redis Hash
- **字段**：
  - `id`：任务ID
  - `status`：任务状态
  - `progress`：转码进度
  - `speed`：转码速度
  - `estimatedTime`：预计完成时间

#### 4.2.3 任务状态索引
- **名称**：`transcode:tasks:{status}`
- **类型**：Redis Set
- **结构**：存储对应状态的任务ID
- **操作**：SADD、SREM、SMEMBERS 等

#### 4.2.4 任务优先级队列
- **名称**：`transcode:queue:priority:{priority}`
- **类型**：Redis List 或 Redisson RBlockingQueue
- **结构**：按优先级存储任务ID

#### 4.2.5 策略缓存
- **名称**：`transcode:strategy:{strategyId}`
- **类型**：Redis Hash
- **结构**：缓存策略详情
- **过期时间**：1小时

## 5. API 接口

### 5.1 任务入队接口
- **路径**：`/api/transcode/task/queue`
- **方法**：POST
- **请求体**：
  ```json
  {
    "inputFile": "string",
    "outputFile": "string",
    "strategyId": "string",
    "priority": 5
  }
  ```
- **响应**：
  ```json
  {
    "taskId": "string",
    "status": "PENDING",
    "queuedAt": "timestamp"
  }
  ```

### 5.2 任务出队接口
- **路径**：`/api/transcode/task/dequeue`
- **方法**：GET
- **参数**：
  - `blocking`：是否阻塞（默认false）
  - `timeout`：阻塞超时时间（毫秒，默认0）
- **响应**：
  ```json
  {
    "taskId": "string",
    "inputFile": "string",
    "outputFile": "string",
    "strategyId": "string",
    "status": "PROCESSING"
  }
  ```

### 5.3 任务取消接口
- **路径**：`/api/transcode/task/{taskId}/cancel`
- **方法**：POST
- **响应**：
  ```json
  {
    "taskId": "string",
    "status": "CANCELLED",
    "cancelledAt": "timestamp"
  }
  ```

### 5.4 队列状态接口
- **路径**：`/api/transcode/queue/status`
- **方法**：GET
- **响应**：
  ```json
  {
    "queueLength": 10,
    "pendingTasks": 8,
    "processingTasks": 2,
    "completedTasks": 50,
    "failedTasks": 2
  }
  ```

## 6. 实现方案

### 6.1 技术选型
- **Redis 客户端**：Spring Data Redis Reactive
- **分布式队列**：Redisson RBlockingQueue
- **分布式锁**：Redisson RLock
- **序列化**：JSON 或 MessagePack

### 6.2 核心组件
- **TaskQueueService**：任务队列服务，提供入队、出队等操作
- **TaskManager**：任务管理器，处理任务状态更新
- **QueueMonitor**：队列监控器，监控队列状态
- **PriorityQueueManager**：优先级队列管理器，处理不同优先级的任务

### 6.3 关键流程

#### 6.3.1 任务入队流程
1. 生成唯一任务ID
2. 创建任务数据并存储到 Redis Hash
3. 将任务ID添加到对应优先级的队列
4. 将任务ID添加到 PENDING 状态集合
5. 返回任务ID和状态

#### 6.3.2 任务出队流程
1. 从高优先级队列开始尝试获取任务
2. 获取到任务后，将其从队列中移除
3. 更新任务状态为 PROCESSING
4. 将任务ID从 PENDING 状态集合移动到 PROCESSING 状态集合
5. 返回任务信息

#### 6.3.3 任务处理流程
1. 从队列获取任务
2. 执行转码操作
3. 实时更新任务进度
4. 转码完成后更新任务状态为 COMPLETED
5. 转码失败后更新任务状态为 FAILED
6. 将任务ID从 PROCESSING 状态集合移动到对应状态集合

## 7. 监控与告警

### 7.1 监控指标
- 队列长度
- 任务处理速率
- 任务平均处理时间
- 任务失败率
- 队列积压程度
- Redis 内存使用情况

### 7.2 告警机制
- 队列长度超过阈值
- 任务失败率超过阈值
- 队列积压时间过长
- Redis 内存使用过高
- 服务不可用

## 8. 部署与运维

### 8.1 依赖
- Redis 6.0+
- JDK 25+
- Spring Boot 3.x

### 8.2 配置
- Redis 连接信息
- 队列名称前缀
- 任务超时设置
- 最大重试次数
- 并发处理线程数

### 8.3 运维操作
- 队列清理
- 任务重试
- 服务重启策略
- 故障恢复流程

## 9. 测试计划

### 9.1 单元测试
- 任务入队/出队测试
- 任务状态更新测试
- 队列管理测试
- 优先级调度测试

### 9.2 集成测试
- 与转码引擎的集成测试
- 与 Redis 的集成测试
- 与 H2 数据库的集成测试
- 与 MySQL 数据库的集成测试
- 分布式部署测试
- 故障恢复测试

### 9.3 性能测试
- 并发入队/出队测试
- 队列积压测试
- 高负载测试
- 响应时间测试

### 9.4 数据库兼容性测试
- H2 与 MySQL 兼容性测试
- 自动迁移测试
- 数据一致性测试
- 数据库切换测试

## 10. 风险评估

### 10.1 潜在风险
- Redis 服务故障
- 任务数据丢失
- 队列积压
- 性能瓶颈
- 分布式部署冲突

### 10.2 缓解措施
- Redis 集群部署
- 任务数据持久化
- 队列监控与告警
- 性能优化
- 分布式锁的合理使用