# Transcode Strategy 规范

## 1. 概述

本规范定义了转码策略管理系统，用于配置和管理不同场景下的转码参数。转码策略是一组预定义的转码参数集合，包括目标格式、分辨率、码率、编码器等配置，用于指导转码引擎执行具体的转码操作。

## 2. 功能需求

### 2.1 策略管理
- **策略创建**：支持创建新的转码策略，包括设置策略名称、目标格式、分辨率、码率等参数
- **策略查询**：支持查询策略列表和单个策略详情
- **策略更新**：支持修改现有策略的参数
- **策略删除**：支持删除不再使用的策略
- **策略复制**：支持基于现有策略创建新策略

### 2.2 策略类型
- **预设策略**：系统内置的常用转码策略，如高清、标清、流畅等
- **自定义策略**：用户根据具体需求创建的自定义策略
- **场景策略**：针对特定场景优化的策略，如直播、点播、移动设备等

### 2.3 策略参数
- **视频参数**：
  - 目标格式（如 MP4、MKV、WebM 等）
  - 分辨率（如 1080p、720p、480p 等）
  - 码率（如 2Mbps、1Mbps、500Kbps 等）
  - 帧率（如 30fps、25fps、24fps 等）
  - 编码器（如 H.264、H.265、VP9、AV1 等）
  - 编码预设（如 ultrafast、fast、medium、slow 等）
  - 质量参数（如 CRF 值）

- **音频参数**：
  - 编码器（如 AAC、MP3、Opus、Vorbis 等）
  - 比特率（如 128Kbps、192Kbps、320Kbps 等）
  - 采样率（如 44.1kHz、48kHz 等）
  - 声道数（如单声道、立体声、5.1 环绕声等）

- **容器参数**：
  - 容器格式（如 MP4、MKV、WebM 等）
  - 元数据设置
  - 封装选项

### 2.4 策略验证
- **参数验证**：验证策略参数的有效性和合法性
- **兼容性检查**：检查参数组合的兼容性
- **预估效果**：根据策略参数预估转码后的文件大小和质量

### 2.5 策略应用
- **策略选择**：支持根据输入文件特性自动选择合适的策略
- **策略覆盖**：支持在创建转码任务时覆盖策略的特定参数
- **策略优先级**：支持为策略设置优先级

## 3. 非功能需求

### 3.1 性能
- 策略查询响应时间：< 50ms
- 策略创建/更新响应时间：< 100ms
- 支持至少 1000 个自定义策略

### 3.2 可靠性
- 策略数据持久化存储
- 策略版本管理
- 策略变更的审计日志

### 3.3 可扩展性
- 支持新增策略参数类型
- 支持插件式策略扩展
- 支持策略模板导入/导出

### 3.4 安全性
- 策略数据的访问控制
- 避免恶意策略参数导致的安全问题
- 策略参数的合法性验证

## 4. 数据模型

### 4.1 数据库表

#### 4.1.1 transcode_strategy 表
| 字段名 | 数据类型 | 约束 | 描述 |
|-------|---------|------|------|
| id | VARCHAR(36) | PRIMARY KEY | 策略ID |
| name | VARCHAR(100) | NOT NULL | 策略名称 |
| type | VARCHAR(20) | NOT NULL | 策略类型（PRESET、CUSTOM、SCENE） |
| target_format | VARCHAR(50) | NOT NULL | 目标格式 |
| resolution | VARCHAR(20) | NOT NULL | 目标分辨率 |
| bitrate | VARCHAR(20) | NOT NULL | 目标码率 |
| encoder | VARCHAR(50) | NOT NULL | 编码器 |
| preset | VARCHAR(50) | NOT NULL | 编码预设 |
| video_codec | VARCHAR(50) | NOT NULL | 视频编码器 |
| video_bitrate | VARCHAR(20) | NOT NULL | 视频码率 |
| video_framerate | INT | NOT NULL | 视频帧率 |
| video_crf | INT | NULL | 视频质量参数（CRF） |
| audio_codec | VARCHAR(50) | NOT NULL | 音频编码器 |
| audio_bitrate | VARCHAR(20) | NOT NULL | 音频比特率 |
| audio_samplerate | INT | NOT NULL | 音频采样率 |
| audio_channels | INT | NOT NULL | 音频声道数 |
| container_format | VARCHAR(50) | NOT NULL | 容器格式 |
| description | TEXT | NULL | 策略描述 |
| is_enabled | BOOLEAN | NOT NULL DEFAULT TRUE | 是否启用 |
| created_at | DATETIME | NOT NULL | 创建时间 |
| updated_at | DATETIME | NOT NULL | 更新时间 |
| created_by | VARCHAR(36) | NULL | 创建人 |
| updated_by | VARCHAR(36) | NULL | 更新人 |

### 4.2 Redis 缓存

#### 4.2.1 策略缓存
- **名称**：`transcode:strategy:{strategyId}`
- **类型**：Redis Hash
- **结构**：缓存策略详情
- **过期时间**：1小时

#### 4.2.2 策略列表缓存
- **名称**：`transcode:strategies:all`
- **类型**：Redis Set
- **结构**：存储所有策略ID
- **过期时间**：30分钟

#### 4.2.3 策略类型缓存
- **名称**：`transcode:strategies:{type}`
- **类型**：Redis Set
- **结构**：存储指定类型的策略ID
- **过期时间**：30分钟

## 5. API 接口

### 5.1 策略管理接口

#### 5.1.1 创建策略
- **路径**：`/api/transcode/strategy`
- **方法**：POST
- **请求体**：
  ```json
  {
    "name": "高清策略",
    "type": "CUSTOM",
    "targetFormat": "mp4",
    "resolution": "1920x1080",
    "bitrate": "2000k",
    "encoder": "libx264",
    "preset": "medium",
    "videoCodec": "libx264",
    "videoBitrate": "1800k",
    "videoFramerate": 30,
    "videoCrf": 23,
    "audioCodec": "aac",
    "audioBitrate": "128k",
    "audioSamplerate": 44100,
    "audioChannels": 2,
    "containerFormat": "mp4",
    "description": "适用于高清视频转码"
  }
  ```
- **响应**：
  ```json
  {
    "id": "strategy-001",
    "name": "高清策略",
    "type": "CUSTOM",
    "status": "CREATED",
    "createdAt": "2026-02-13T10:00:00Z"
  }
  ```

#### 5.1.2 查询策略列表
- **路径**：`/api/transcode/strategy`
- **方法**：GET
- **参数**：
  - `type`：策略类型（可选）
  - `enabled`：是否启用（可选）
  - `page`：页码（默认1）
  - `size`：每页数量（默认10）
- **响应**：
  ```json
  {
    "total": 20,
    "page": 1,
    "size": 10,
    "items": [
      {
        "id": "strategy-001",
        "name": "高清策略",
        "type": "CUSTOM",
        "targetFormat": "mp4",
        "resolution": "1920x1080",
        "isEnabled": true,
        "createdAt": "2026-02-13T10:00:00Z"
      },
      // 更多策略...
    ]
  }
  ```

#### 5.1.3 查询策略详情
- **路径**：`/api/transcode/strategy/{id}`
- **方法**：GET
- **响应**：
  ```json
  {
    "id": "strategy-001",
    "name": "高清策略",
    "type": "CUSTOM",
    "targetFormat": "mp4",
    "resolution": "1920x1080",
    "bitrate": "2000k",
    "encoder": "libx264",
    "preset": "medium",
    "videoCodec": "libx264",
    "videoBitrate": "1800k",
    "videoFramerate": 30,
    "videoCrf": 23,
    "audioCodec": "aac",
    "audioBitrate": "128k",
    "audioSamplerate": 44100,
    "audioChannels": 2,
    "containerFormat": "mp4",
    "description": "适用于高清视频转码",
    "isEnabled": true,
    "createdAt": "2026-02-13T10:00:00Z",
    "updatedAt": "2026-02-13T10:00:00Z"
  }
  ```

#### 5.1.4 更新策略
- **路径**：`/api/transcode/strategy/{id}`
- **方法**：PUT
- **请求体**：
  ```json
  {
    "name": "高清策略（优化）",
    "videoBitrate": "1500k",
    "audioBitrate": "96k",
    "description": "优化后的高清视频转码策略"
  }
  ```
- **响应**：
  ```json
  {
    "id": "strategy-001",
    "name": "高清策略（优化）",
    "status": "UPDATED",
    "updatedAt": "2026-02-13T11:00:00Z"
  }
  ```

#### 5.1.5 删除策略
- **路径**：`/api/transcode/strategy/{id}`
- **方法**：DELETE
- **响应**：
  ```json
  {
    "id": "strategy-001",
    "status": "DELETED",
    "deletedAt": "2026-02-13T12:00:00Z"
  }
  ```

#### 5.1.6 复制策略
- **路径**：`/api/transcode/strategy/{id}/copy`
- **方法**：POST
- **请求体**：
  ```json
  {
    "name": "高清策略（副本）"
  }
  ```
- **响应**：
  ```json
  {
    "id": "strategy-002",
    "name": "高清策略（副本）",
    "status": "CREATED",
    "createdAt": "2026-02-13T13:00:00Z"
  }
  ```

## 6. 实现方案

### 6.1 技术选型
- **数据存储**：MySQL 数据库 + Redis 缓存
- **ORM 框架**：MyBatis-Plus
- **缓存框架**：Spring Cache + Redis
- **验证框架**：Spring Validation

### 6.2 核心组件
- **StrategyService**：策略管理服务，提供策略的增删改查等操作
- **StrategyValidator**：策略参数验证器，验证策略参数的有效性
- **StrategyCache**：策略缓存管理器，管理策略的缓存
- **PresetStrategyLoader**：预设策略加载器，加载系统内置策略
- **StrategyOptimizer**：策略优化器，根据输入文件特性优化策略参数

### 6.3 关键流程

#### 6.3.1 策略创建流程
1. 接收策略创建请求
2. 验证策略参数的有效性
3. 检查策略名称是否重复
4. 生成策略ID
5. 保存策略到数据库
6. 更新 Redis 缓存
7. 返回策略创建结果

#### 6.3.2 策略应用流程
1. 转码任务创建时选择或自动推荐策略
2. 从数据库或缓存中获取策略详情
3. 根据策略参数构建转码命令
4. 执行转码操作
5. 记录策略使用情况

#### 6.3.3 策略优化流程
1. 分析输入文件的特性（分辨率、码率、格式等）
2. 根据目标场景选择基础策略
3. 调整策略参数以适应输入文件
4. 预估转码结果
5. 应用优化后的策略

## 7. 监控与告警

### 7.1 监控指标
- 策略使用次数
- 策略创建/更新/删除操作数
- 策略缓存命中率
- 策略验证失败率

### 7.2 告警机制
- 策略参数验证失败
- 策略缓存异常
- 策略使用异常

## 8. 部署与运维

### 8.1 依赖
- MySQL 8.0+
- Redis 6.0+
- JDK 25+
- Spring Boot 3.x

### 8.2 配置
- 数据库连接信息
- Redis 连接信息
- 缓存过期时间
- 预设策略配置

### 8.3 运维操作
- 预设策略管理
- 策略备份与恢复
- 策略性能分析
- 策略使用统计

## 9. 测试计划

### 9.1 单元测试
- 策略创建/更新/删除测试
- 策略参数验证测试
- 策略缓存测试
- 预设策略加载测试

### 9.2 集成测试
- 策略与转码引擎集成测试
- 策略与任务管理集成测试
- 策略与缓存集成测试

### 9.3 性能测试
- 策略查询性能测试
- 策略创建/更新性能测试
- 缓存命中率测试
- 大规模策略管理测试

## 10. 风险评估

### 10.1 潜在风险
- **策略参数冲突**：不同参数之间可能存在冲突，导致转码失败
- **策略性能问题**：某些策略参数可能导致转码速度过慢
- **策略兼容性问题**：某些策略可能在特定环境下不兼容
- **缓存一致性问题**：数据库与缓存之间可能存在数据不一致

### 10.2 缓解措施
- **参数验证**：严格验证策略参数的有效性和兼容性
- **性能测试**：对策略进行性能测试，确保转码速度合理
- **兼容性测试**：在不同环境下测试策略的兼容性
- **缓存同步**：确保数据库与缓存之间的数据同步
- **策略版本管理**：支持策略的版本控制，便于回滚
- **异常处理**：完善的异常处理机制，确保策略管理系统的稳定性