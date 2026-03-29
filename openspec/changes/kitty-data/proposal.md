## Why

当前系统缺少统一的事件上报和统计分析能力，需要构建一个轻量级的事件上报服务 kitty-data，用于收集、存储和分析各类业务事件，提供实时统计和监控能力。

## What Changes

- **新建** kitty-data 服务：轻量化的事件上报和统计分析服务
- **删除** 原有的转码相关逻辑
- **移除** gorm、grpc 等依赖
- **引入** viper（配置管理）、zap（日志）、mongo（数据存储）

## Capabilities

### New Capabilities

- **ak-sk-auth**: AK/SK 鉴权服务
  - 本地缓存 AK/SK 全量数据，支持高性能鉴权
  - 定时任务自动刷新缓存
  - 控制面接口支持手动触发缓存更新

- **signature-auth**: 签名鉴权中间件
  - 基于 X-AK、X-SIGN、X-TIMESTAMP 的签名验证
  - 支持时间戳防重放，允许配置正负抖动范围

- **event-report**: 事件上报接口
  - 记录事件类型、操作者、操作值
  - 操作值支持普通操作（固定值1）和用量操作（任意整数）

- **event-statistics**: 统计分析接口
  - 时间范围内事件触发次数/操作值总和
  - 操作者排名统计
  - 多事件组合统计
  - 按时间维度（年/月/周/日/小时）的趋势分析

- **event-monitor**: 实时监控接口
  - SSE 流式输出指定事件和用户的监控数据

### Modified Capabilities

- 无

## Impact

- 新增服务：kitty-data
- 依赖：viper、zap、mongo-driver
- 新增接口：
  - 事件上报 POST /api/v1/events
  - 统计查询 GET/POST 多个统计接口
  - 缓存刷新 POST /api/v1/auth/refresh
  - SSE 监控 /api/v1/events/monitor
- **API 风格**：标准 RESTful，返回值无通用 DTO 包装（无泛型包装器如 Result/Response）
