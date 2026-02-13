# Transcode Concurrency Control 规范

## 1. 概述

本规范定义了转码服务的多实例并发控制机制，用于确保在多实例环境下，转码任务不会被多个实例重复处理。并发控制模块使用分布式锁和任务状态管理，确保任务的唯一性和一致性。

## 2. 功能需求

### 2.1 分布式锁
- **任务锁**：为每个转码任务创建分布式锁
- **锁获取**：任务执行前获取锁，失败则等待或跳过
- **锁释放**：任务完成后释放锁
- **锁超时**：设置锁的超时时间，防止死锁
- **锁重试**：锁获取失败时自动重试

### 2.2 任务分配
- **任务抢占**：多个实例竞争获取任务
- **任务绑定**：任务分配给特定实例后，其他实例不能处理
- **任务转移**：实例故障时，任务可以转移到其他实例
- **负载均衡**：根据实例负载情况分配任务

### 2.3 实例管理
- **实例注册**：实例启动时注册到系统
- **实例心跳**：定期发送心跳，保持实例活跃状态
- **实例发现**：发现其他实例的存在
- **实例下线**：实例下线时释放所有任务

### 2.4 任务状态同步
- **状态更新**：任务状态变更时同步到所有实例
- **状态查询**：查询任务的最新状态
- **状态冲突**：处理状态冲突的情况
- **状态一致性**：确保所有实例看到一致的任务状态

### 2.5 故障恢复
- **实例故障检测**：检测实例是否故障
- **任务恢复**：恢复故障实例的任务
- **任务重试**：故障任务自动重试
- **数据恢复**：恢复未完成的任务数据

## 3. 非功能需求

### 3.1 性能
- 锁获取响应时间：< 100ms
- 锁释放响应时间：< 50ms
- 任务分配延迟：< 1s
- 状态同步延迟：< 500ms

### 3.2 可靠性
- 锁获取成功率：> 99%
- 任务分配成功率：> 99%
- 故障恢复时间：< 30s
- 数据一致性：100%

### 3.3 可扩展性
- 支持至少 10 个实例并发
- 支持动态增减实例
- 支持水平扩展

### 3.4 安全性
- 锁的权限控制
- 实例的身份验证
- 防止锁的恶意占用

## 4. 数据模型

### 4.1 Redis 数据结构

#### 4.1.1 任务锁
- **名称**：`transcode:task:lock:{taskId}`
- **类型**：Redis String（用于分布式锁）
- **结构**：存储锁的持有者信息和过期时间
- **TTL**：锁的超时时间（默认 30 分钟）

#### 4.1.2 实例注册表
- **名称**：`transcode:instances`
- **类型**：Redis Set
- **结构**：存储所有活跃实例的 ID

#### 4.1.3 实例信息
- **名称**：`transcode:instance:{instanceId}`
- **类型**：Redis Hash
- **结构**：
  - `host`：实例主机地址
  - `port`：实例端口
  - `startTime`：启动时间
  - `lastHeartbeat`：最后心跳时间
  - `taskCount`：当前任务数量
  - `status`：实例状态（ACTIVE、BUSY、OFFLINE）

#### 4.1.4 实例任务映射
- **名称**：`transcode:instance:tasks:{instanceId}`
- **类型**：Redis Set
- **结构**：存储实例当前处理的任务 ID

#### 4.1.5 任务实例映射
- **名称**：`transcode:task:instance:{taskId}`
- **类型**：Redis String
- **结构**：存储处理该任务的实例 ID

#### 4.1.6 心跳时间戳
- **名称**：`transcode:heartbeat:{instanceId}`
- **类型**：Redis String
- **结构**：存储最后心跳时间戳
- **TTL**：心跳超时时间（默认 60 秒）

## 5. API 接口

### 5.1 实例管理接口

#### 5.1.1 实例注册
- **路径**：`/api/transcode/instance/register`
- **方法**：POST
- **请求体**：
  ```json
  {
    "instanceId": "string",
    "host": "string",
    "port": 8080
  }
  ```
- **响应**：
  ```json
  {
    "instanceId": "string",
    "status": "REGISTERED",
    "registeredAt": "timestamp"
  }
  ```

#### 5.1.2 实例心跳
- **路径**：`/api/transcode/instance/heartbeat`
- **方法**：POST
- **请求体**：
  ```json
  {
    "instanceId": "string",
    "taskCount": 5
  }
  ```
- **响应**：
  ```json
  {
    "instanceId": "string",
    "status": "ALIVE",
    "heartbeatAt": "timestamp"
  }
  ```

#### 5.1.3 实例下线
- **路径**：`/api/transcode/instance/unregister`
- **方法**：DELETE
- **请求体**：
  ```json
  {
    "instanceId": "string"
  }
  ```
- **响应**：
  ```json
  {
    "instanceId": "string",
    "status": "UNREGISTERED",
    "unregisteredAt": "timestamp"
  }
  ```

#### 5.1.4 查询实例列表
- **路径**：`/api/transcode/instance`
- **方法**：GET
- **响应**：
  ```json
  {
    "total": 5,
    "items": [
      {
        "instanceId": "string",
        "host": "string",
        "port": 8080,
        "startTime": "timestamp",
        "lastHeartbeat": "timestamp",
        "taskCount": 5,
        "status": "ACTIVE"
      }
    ]
  }
  ```

### 5.2 锁管理接口

#### 5.2.1 获取任务锁
- **路径**：`/api/transcode/lock/acquire`
- **方法**：POST
- **请求体**：
  ```json
  {
    "taskId": "string",
    "instanceId": "string",
    "timeout": 1800
  }
  ```
- **响应**：
  ```json
  {
    "taskId": "string",
    "instanceId": "string",
    "locked": true,
    "lockedAt": "timestamp",
    "expiresAt": "timestamp"
  }
  ```

#### 5.2.2 释放任务锁
- **路径**：`/api/transcode/lock/release`
- **方法**：POST
- **请求体**：
  ```json
  {
    "taskId": "string",
    "instanceId": "string"
  }
  ```
- **响应**：
  ```json
  {
    "taskId": "string",
    "instanceId": "string",
    "released": true,
    "releasedAt": "timestamp"
  }
  ```

#### 5.2.3 查询锁状态
- **路径**：`/api/transcode/lock/{taskId}`
- **方法**：GET
- **响应**：
  ```json
  {
    "taskId": "string",
    "locked": true,
    "instanceId": "string",
    "lockedAt": "timestamp",
    "expiresAt": "timestamp"
  }
  ```

## 6. 实现方案

### 6.1 技术选型
- **分布式锁**：Redisson RLock
- **实例注册**：Redis Set
- **心跳检测**：Redis String with TTL
- **状态同步**：Redis Pub/Sub

### 6.2 核心组件
- **InstanceManager**：实例管理器
- **LockManager**：锁管理器
- **HeartbeatService**：心跳服务
- **TaskAllocator**：任务分配器
- **FailureDetector**：故障检测器
- **TaskRecoveryService**：任务恢复服务

### 6.3 关键流程

#### 6.3.1 实例启动流程
1. 生成实例 ID
2. 注册实例到 Redis
3. 启动心跳服务
4. 启动故障检测服务
5. 开始处理任务

#### 6.3.2 任务分配流程
1. 从任务队列中获取任务
2. 尝试获取任务锁
3. 如果获取成功：
   - 将任务分配给当前实例
   - 更新任务状态为 PROCESSING
   - 开始处理任务
4. 如果获取失败：
   - 跳过该任务
   - 处理下一个任务

#### 6.3.3 任务处理流程
1. 获取任务锁
2. 更新任务状态为 PROCESSING
3. 执行转码任务
4. 更新任务状态为 COMPLETED 或 FAILED
5. 释放任务锁
6. 从实例任务列表中移除任务

#### 6.3.4 实例下线流程
1. 停止接收新任务
2. 等待当前任务完成
3. 释放所有任务锁
4. 从实例注册表中移除
5. 清理实例信息

#### 6.3.5 故障恢复流程
1. 检测到实例故障
2. 查找故障实例的任务
3. 将任务状态重置为 PENDING
4. 释放任务锁
5. 任务重新进入队列
6. 其他实例可以重新获取任务

## 7. 监控与告警

### 7.1 监控指标
- 实例数量
- 每个实例的任务数量
- 锁获取成功率
- 锁获取延迟
- 心跳成功率
- 故障恢复次数

### 7.2 告警机制
- 实例下线
- 锁获取失败率过高
- 心跳超时
- 任务分配失败

## 8. 部署与运维

### 8.1 依赖
- Redis 6.0+
- JDK 25+
- Spring Boot 3.x

### 8.2 配置
- Redis 连接信息
- 锁超时时间
- 心跳间隔
- 心跳超时时间
- 故障检测间隔
- 任务分配策略

### 8.3 运维操作
- 查看实例列表
- 查看锁状态
- 手动释放锁
- 手动下线实例
- 查看任务分配情况

## 9. 测试计划

### 9.1 单元测试
- 锁获取和释放测试
- 实例注册和下线测试
- 心跳服务测试
- 任务分配测试
- 故障恢复测试

### 9.2 集成测试
- 多实例并发测试
- 锁冲突测试
- 实例故障测试
- 任务恢复测试
- 状态同步测试

### 9.3 性能测试
- 锁获取性能测试
- 任务分配性能测试
- 多实例负载测试
- 心跳性能测试

### 9.4 边界测试
- 锁超时测试
- 实例频繁上下线测试
- 网络分区测试
- Redis 故障测试

## 10. 风险评估

### 10.1 潜在风险
- **锁超时**：任务执行时间超过锁超时时间，导致锁被其他实例获取
- **死锁**：多个实例互相等待锁，导致死锁
- **实例故障**：实例故障导致任务无法完成
- **网络分区**：网络分区导致实例无法通信
- **Redis 故障**：Redis 故障导致分布式锁失效

### 10.2 缓解措施
- **锁续期**：任务执行过程中定期续期锁
- **锁超时设置**：根据任务执行时间设置合理的锁超时
- **故障检测**：及时检测实例故障，恢复任务
- **心跳机制**：定期发送心跳，检测实例状态
- **Redis 高可用**：使用 Redis 集群或哨兵模式，提高可用性