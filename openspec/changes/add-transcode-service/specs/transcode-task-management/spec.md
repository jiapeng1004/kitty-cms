# Transcode Task Management 规范

## 1. 概述

本规范定义了转码任务的全生命周期管理系统，用于处理转码任务的创建、状态跟踪、进度查询、任务取消、失败重试等功能。转码任务管理是连接用户请求、任务队列和转码引擎的核心组件，确保转码任务能够被正确处理和监控。

## 2. 功能需求

### 2.1 任务管理
- **任务创建**：支持创建新的转码任务，包括设置输入文件、输出文件、转码策略等参数
- **任务查询**：支持查询单个任务详情和任务列表，包括任务状态、进度、结果等信息
- **任务取消**：支持取消正在处理的转码任务
- **任务重试**：支持重试失败的转码任务
- **任务删除**：支持删除已完成或已取消的转码任务

### 2.2 状态管理
- **状态定义**：
  - PENDING：任务已创建，等待处理
  - PROCESSING：任务正在处理中
  - COMPLETED：任务处理完成
  - FAILED：任务处理失败
  - CANCELLED：任务已取消
- **状态转换**：定义任务状态之间的合法转换关系
- **状态持久化**：确保任务状态的持久化存储
- **状态同步**：确保数据库和缓存中的任务状态同步

### 2.3 进度管理
- **进度跟踪**：实时跟踪转码任务的进度
- **进度计算**：根据转码时间和文件大小计算进度
- **进度查询**：支持查询任务的当前进度
- **进度通知**：支持进度变化的通知机制

### 2.4 任务调度
- **任务优先级**：支持设置和管理任务优先级
- **任务队列**：支持任务的排队和调度
- **负载均衡**：支持多转码节点的负载均衡
- **资源管理**：支持根据系统资源动态调整任务处理

### 2.5 错误处理
- **错误检测**：检测转码过程中的错误
- **错误分类**：对错误进行分类，便于处理和分析
- **错误重试**：支持自动重试失败的任务
- **错误通知**：支持错误信息的通知和记录

### 2.6 历史记录
- **任务归档**：将已完成的任务归档为历史记录
- **历史查询**：支持查询历史转码记录
- **统计分析**：支持对历史数据进行统计分析
- **趋势分析**：分析转码任务的趋势和模式

## 3. 非功能需求

### 3.1 性能
- 任务创建响应时间：< 100ms
- 任务查询响应时间：< 50ms
- 进度更新频率：至少每秒一次
- 支持每秒至少 100 个任务的处理能力

### 3.2 可靠性
- 任务状态的一致性保证
- 任务数据的持久化存储
- 服务重启后任务状态的恢复
- 网络异常情况下的任务处理

### 3.3 可扩展性
- 支持水平扩展转码节点
- 支持动态调整任务队列大小
- 支持插件式扩展任务处理逻辑
- 支持自定义任务处理器

### 3.4 安全性
- 任务数据的安全存储
- 任务操作的权限控制
- 防止恶意任务导致的安全问题
- 输入文件的安全性检查

## 4. 数据模型

### 4.1 数据库表

#### 4.1.1 transcode_task 表
| 字段名 | 数据类型 | 约束 | 描述 |
|-------|---------|------|------|
| id | VARCHAR(36) | PRIMARY KEY | 任务ID |
| status | VARCHAR(20) | NOT NULL | 任务状态 |
| progress | INT | NOT NULL DEFAULT 0 | 转码进度（0-100） |
| input_file | VARCHAR(500) | NOT NULL | 输入文件路径 |
| output_file | VARCHAR(500) | NOT NULL | 输出文件路径 |
| strategy_id | VARCHAR(36) | NOT NULL | 转码策略ID |
| priority | INT | NOT NULL DEFAULT 5 | 任务优先级（1-10） |
| error_message | TEXT | NULL | 错误信息 |
| retry_count | INT | NOT NULL DEFAULT 0 | 重试次数 |
| max_retries | INT | NOT NULL DEFAULT 3 | 最大重试次数 |
| created_at | DATETIME | NOT NULL | 创建时间 |
| started_at | DATETIME | NULL | 开始时间 |
| completed_at | DATETIME | NULL | 完成时间 |
| cancelled_at | DATETIME | NULL | 取消时间 |
| created_by | VARCHAR(36) | NULL | 创建人 |

#### 4.1.2 transcode_history 表
| 字段名 | 数据类型 | 约束 | 描述 |
|-------|---------|------|------|
| id | VARCHAR(36) | PRIMARY KEY | 历史记录ID |
| task_id | VARCHAR(36) | NOT NULL | 任务ID |
| status | VARCHAR(20) | NOT NULL | 任务状态 |
| progress | INT | NOT NULL | 转码进度 |
| input_file | VARCHAR(500) | NOT NULL | 输入文件路径 |
| output_file | VARCHAR(500) | NOT NULL | 输出文件路径 |
| strategy_id | VARCHAR(36) | NOT NULL | 转码策略ID |
| error_message | TEXT | NULL | 错误信息 |
| retry_count | INT | NOT NULL | 重试次数 |
| duration | INT | NULL | 转码时长（秒） |
| input_size | BIGINT | NULL | 输入文件大小（字节） |
| output_size | BIGINT | NULL | 输出文件大小（字节） |
| created_at | DATETIME | NOT NULL | 创建时间 |
| started_at | DATETIME | NULL | 开始时间 |
| completed_at | DATETIME | NULL | 完成时间 |

### 4.2 Redis 数据结构

#### 4.2.1 任务实时状态
- **名称**：`transcode:task:{taskId}`
- **类型**：Redis Hash
- **字段**：
  - `id`：任务ID
  - `status`：任务状态
  - `progress`：转码进度
  - `speed`：转码速度
  - `estimatedTime`：预计完成时间
  - `currentTime`：当前处理时间
  - `totalTime`：总处理时间

#### 4.2.2 任务队列
- **名称**：`transcode:queue`
- **类型**：Redis List 或 Redisson RBlockingQueue
- **结构**：存储待处理任务ID

#### 4.2.3 任务状态索引
- **名称**：`transcode:tasks:{status}`
- **类型**：Redis Set
- **结构**：存储对应状态的任务ID

#### 4.2.4 任务锁
- **名称**：`transcode:lock:{taskId}`
- **类型**：Redis String（用于分布式锁）
- **结构**：存储锁的持有者信息

## 5. API 接口

### 5.1 任务管理接口

#### 5.1.1 创建任务
- **路径**：`/api/transcode/task`
- **方法**：POST
- **请求体**：
  ```json
  {
    "inputFile": "string",
    "outputFile": "string",
    "strategyId": "string",
    "priority": 5,
    "maxRetries": 3
  }
  ```
- **响应**：
  ```json
  {
    "taskId": "string",
    "status": "PENDING",
    "createdAt": "timestamp"
  }
  ```

#### 5.1.2 查询任务
- **路径**：`/api/transcode/task/{id}`
- **方法**：GET
- **响应**：
  ```json
  {
    "id": "string",
    "status": "PROCESSING",
    "progress": 45,
    "inputFile": "string",
    "outputFile": "string",
    "strategyId": "string",
    "priority": 5,
    "speed": 2.5,
    "estimatedTime": 10,
    "createdAt": "timestamp",
    "startedAt": "timestamp"
  }
  ```

#### 5.1.3 查询任务列表
- **路径**：`/api/transcode/task`
- **方法**：GET
- **参数**：
  - `status`：任务状态（可选）
  - `page`：页码（默认1）
  - `size`：每页数量（默认10）
  - `sort`：排序字段（默认createdAt）
  - `order`：排序方向（默认desc）
- **响应**：
  ```json
  {
    "total": 100,
    "page": 1,
    "size": 10,
    "items": [
      {
        "id": "string",
        "status": "PROCESSING",
        "progress": 45,
        "inputFile": "string",
        "outputFile": "string",
        "strategyId": "string",
        "createdAt": "timestamp"
      },
      // 更多任务...
    ]
  }
  ```

#### 5.1.4 取消任务
- **路径**：`/api/transcode/task/{id}/cancel`
- **方法**：POST
- **响应**：
  ```json
  {
    "taskId": "string",
    "status": "CANCELLED",
    "cancelledAt": "timestamp"
  }
  ```

#### 5.1.5 重试任务
- **路径**：`/api/transcode/task/{id}/retry`
- **方法**：POST
- **响应**：
  ```json
  {
    "taskId": "string",
    "status": "PENDING",
    "retryCount": 1,
    "retriedAt": "timestamp"
  }
  ```

#### 5.1.6 删除任务
- **路径**：`/api/transcode/task/{id}`
- **方法**：DELETE
- **响应**：
  ```json
  {
    "taskId": "string",
    "status": "DELETED",
    "deletedAt": "timestamp"
  }
  ```

### 5.2 进度管理接口

#### 5.2.1 查询任务进度
- **路径**：`/api/transcode/task/{id}/progress`
- **方法**：GET
- **响应**：
  ```json
  {
    "taskId": "string",
    "progress": 45,
    "speed": 2.5,
    "estimatedTime": 10,
    "currentTime": 22.5,
    "totalTime": 50
  }
  ```

### 5.3 历史记录接口

#### 5.3.1 查询历史记录
- **路径**：`/api/transcode/history`
- **方法**：GET
- **参数**：
  - `startDate`：开始日期（可选）
  - `endDate`：结束日期（可选）
  - `status`：任务状态（可选）
  - `page`：页码（默认1）
  - `size`：每页数量（默认10）
- **响应**：
  ```json
  {
    "total": 500,
    "page": 1,
    "size": 10,
    "items": [
      {
        "id": "string",
        "taskId": "string",
        "status": "COMPLETED",
        "inputFile": "string",
        "outputFile": "string",
        "strategyId": "string",
        "duration": 120,
        "inputSize": 104857600,
        "outputSize": 26214400,
        "createdAt": "timestamp",
        "completedAt": "timestamp"
      },
      // 更多历史记录...
    ]
  }
  ```

## 6. 实现方案

### 6.1 技术选型
- **数据存储**：MySQL 数据库 + Redis 缓存
- **队列实现**：Redisson RBlockingQueue
- **分布式锁**：Redisson RLock
- **异步处理**：Spring WebFlux + CompletableFuture
- **ORM 框架**：MyBatis-Plus

### 6.2 核心组件
- **TaskManager**：任务管理器，处理任务的创建、查询、取消等操作
- **TaskScheduler**：任务调度器，管理任务的排队和分发
- **ProgressTracker**：进度跟踪器，实时跟踪任务进度
- **ErrorHandler**：错误处理器，处理任务执行过程中的错误
- **HistoryManager**：历史记录管理器，管理任务的历史记录
- **TaskExecutor**：任务执行器，执行具体的转码任务

### 6.3 关键流程

#### 6.3.1 任务创建流程
1. 接收任务创建请求
2. 验证请求参数的有效性
3. 检查输入文件的存在性
4. 生成任务ID
5. 保存任务到数据库
6. 将任务添加到队列
7. 更新任务状态为 PENDING
8. 返回任务创建结果

#### 6.3.2 任务处理流程
1. 从队列中获取任务
2. 获取任务锁，确保任务不被重复处理
3. 更新任务状态为 PROCESSING
4. 调用转码引擎执行转码
5. 实时跟踪转码进度
6. 根据转码结果更新任务状态
   - 成功：更新为 COMPLETED
   - 失败：更新为 FAILED，根据配置决定是否重试
7. 释放任务锁
8. 发送任务完成通知

#### 6.3.3 任务取消流程
1. 接收任务取消请求
2. 验证任务是否存在且可取消
3. 获取任务锁
4. 更新任务状态为 CANCELLED
5. 从队列中移除任务（如果任务尚未开始处理）
6. 通知转码引擎停止处理
7. 释放任务锁
8. 返回任务取消结果

#### 6.3.4 任务重试流程
1. 接收任务重试请求
2. 验证任务是否存在且可重试
3. 检查重试次数是否超过限制
4. 获取任务锁
5. 重置任务状态为 PENDING
6. 增加重试次数
7. 将任务重新添加到队列
8. 释放任务锁
9. 返回任务重试结果

## 7. 监控与告警

### 7.1 监控指标
- 任务创建率
- 任务完成率
- 任务失败率
- 任务平均处理时间
- 队列长度
- 系统资源使用率
- 任务进度更新频率

### 7.2 告警机制
- 队列积压告警
- 任务失败率过高告警
- 系统资源使用过高告警
- 任务处理超时告警
- 转码引擎异常告警

## 8. 部署与运维

### 8.1 依赖
- MySQL 8.0+
- Redis 6.0+
- JDK 25+
- Spring Boot 3.x
- FFmpeg 4.0+

### 8.2 配置
- 数据库连接信息
- Redis 连接信息
- 转码引擎配置
- 任务队列配置
- 资源限制配置
- 告警阈值配置

### 8.3 运维操作
- 任务监控
- 队列管理
- 错误处理
- 系统扩容
- 故障排查
- 性能调优

## 9. 测试计划

### 9.1 单元测试
- 任务创建/查询/取消测试
- 状态转换测试
- 进度跟踪测试
- 错误处理测试

### 9.2 集成测试
- 与任务队列集成测试
- 与转码引擎集成测试
- 与数据库集成测试
- 与缓存集成测试

### 9.3 性能测试
- 并发任务处理测试
- 队列积压测试
- 系统负载测试
- 故障恢复测试

### 9.4 边界测试
- 大文件转码测试
- 异常文件处理测试
- 网络中断测试
- 服务重启测试

## 10. 风险评估

### 10.1 潜在风险
- **任务积压**：系统负载过高导致任务积压
- **资源耗尽**：转码任务消耗过多系统资源
- **数据不一致**：数据库和缓存中的任务状态不一致
- **任务冲突**：多个转码节点同时处理同一个任务
- **错误重试风暴**：大量任务同时失败导致重试风暴

### 10.2 缓解措施
- **负载均衡**：使用多转码节点进行负载均衡
- **资源限制**：设置转码任务的资源使用限制
- **状态同步**：确保数据库和缓存中的任务状态同步
- **分布式锁**：使用分布式锁防止任务冲突
- **重试策略**：实现指数退避重试策略，避免重试风暴
- **监控告警**：实时监控系统状态，及时发现和处理问题
- **故障隔离**：实现故障隔离机制，防止单个任务失败影响整个系统
- **优雅降级**：在系统负载过高时实现优雅降级，确保核心功能可用