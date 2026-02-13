# Transcode Path Variables 规范

## 1. 概述

本规范定义了转码服务的路径变量功能，用于在输出路径中使用变量替换。路径变量支持动态生成输出路径，包括租户、日期、任务 ID 等变量，提高转码服务的灵活性和可配置性。

## 2. 功能需求

### 2.1 支持的变量
- **?tenant**：租户 ID
- **?year**：当前年份（4 位）
- **?month**：当前月份（2 位，补 0）
- **?day**：当前日期（2 位，补 0）
- **?hour**：当前小时（2 位，补 0）
- **?minute**：当前分钟（2 位，补 0）
- **?second**：当前秒（2 位，补 0）
- **?task_id**：任务 ID
- **?strategy_id**：策略 ID
- **?uuid**：随机 UUID
- **?timestamp**：当前时间戳
- **?date**：当前日期（YYYY-MM-DD）
- **?datetime**：当前日期时间（YYYY-MM-DD HH:mm:ss）

### 2.2 变量替换
- **自动替换**：在路径中自动识别并替换变量
- **变量验证**：验证变量的合法性
- **变量默认值**：支持为变量设置默认值
- **变量格式化**：支持变量的格式化输出

### 2.3 路径规范化
- **路径分隔符**：统一使用 `/` 作为路径分隔符
- **路径去重**：去除连续的路径分隔符
- **路径转义**：转义特殊字符
- **路径验证**：验证路径的合法性

### 2.4 路径生成
- **动态生成**：根据变量动态生成路径
- **路径预览**：预览替换后的路径
- **路径测试**：测试路径是否可用
- **路径冲突检测**：检测路径冲突

### 2.5 自定义变量
- **变量定义**：支持定义自定义变量
- **变量作用域**：支持变量的作用域控制
- **变量优先级**：支持变量的优先级设置
- **变量继承**：支持变量的继承

## 3. 非功能需求

### 3.1 性能
- 变量替换时间：< 10ms
- 路径生成时间：< 20ms
- 路径验证时间：< 10ms

### 3.2 可靠性
- 变量替换成功率：100%
- 路径生成成功率：100%
- 路径验证准确率：100%

### 3.3 可扩展性
- 支持新增变量类型
- 支持自定义变量
- 支持变量格式化

### 3.4 安全性
- 路径遍历防护
- 变量注入防护
- 路径长度限制

## 4. 数据模型

### 4.1 变量定义

#### 4.1.1 内置变量
| 变量名 | 描述 | 示例值 |
|-------|------|--------|
| ?tenant | 租户 ID | tenant_001 |
| ?year | 年份 | 2026 |
| ?month | 月份 | 02 |
| ?day | 日期 | 13 |
| ?hour | 小时 | 14 |
| ?minute | 分钟 | 30 |
| ?second | 秒 | 45 |
| ?task_id | 任务 ID | task_123456 |
| ?strategy_id | 策略 ID | strategy_789 |
| ?uuid | 随机 UUID | 550e8400-e29b-41d4-a716-446655440000 |
| ?timestamp | 时间戳 | 1739457045 |
| ?date | 日期 | 2026-02-13 |
| ?datetime | 日期时间 | 2026-02-13 14:30:45 |

### 4.2 路径示例

#### 4.2.1 基本路径
```
原始路径：/webtv/mms/vod/?tenant/?year/?month/?day/?task_id
替换后：/webtv/mms/vod/tenant_001/2026/02/13/task_123456
```

#### 4.2.2 带媒体类型的路径
```
原始路径：/webtv/mms/vod/?tenant/?year/?month/?day/?task_id_low
替换后：/webtv/mms/vod/tenant_001/2026/02/13/task_123456_low
```

#### 4.2.3 带时间戳的路径
```
原始路径：/webtv/mms/vod/?tenant/?timestamp
替换后：/webtv/mms/vod/tenant_001/1739457045
```

#### 4.2.4 带日期时间的路径
```
原始路径：/webtv/mms/vod/?tenant/?datetime
替换后：/webtv/mms/vod/tenant_001/2026-02-13 14:30:45
```

## 5. API 接口

### 5.1 路径变量接口

#### 5.1.1 替换路径变量
- **路径**：`/api/transcode/path/resolve`
- **方法**：POST
- **请求体**：
  ```json
  {
    "path": "/webtv/mms/vod/?tenant/?year/?month/?day/?task_id",
    "variables": {
      "tenant": "tenant_001",
      "taskId": "task_123456"
    }
  }
  ```
- **响应**：
  ```json
  {
    "originalPath": "/webtv/mms/vod/?tenant/?year/?month/?day/?task_id",
    "resolvedPath": "/webtv/mms/vod/tenant_001/2026/02/13/task_123456",
    "variables": {
      "tenant": "tenant_001",
      "year": "2026",
      "month": "02",
      "day": "13",
      "taskId": "task_123456"
    }
  }
  ```

#### 5.1.2 预览路径
- **路径**：`/api/transcode/path/preview`
- **方法**：POST
- **请求体**：
  ```json
  {
    "path": "/webtv/mms/vod/?tenant/?year/?month/?day/?task_id",
    "variables": {
      "tenant": "tenant_001",
      "taskId": "task_123456"
    }
  }
  ```
- **响应**：
  ```json
  {
    "originalPath": "/webtv/mms/vod/?tenant/?year/?month/?day/?task_id",
    "previewPath": "/webtv/mms/vod/tenant_001/2026/02/13/task_123456",
    "valid": true
  }
  ```

#### 5.1.3 验证路径
- **路径**：`/api/transcode/path/validate`
- **方法**：POST
- **请求体**：
  ```json
  {
    "path": "/webtv/mms/vod/?tenant/?year/?month/?day/?task_id"
  }
  ```
- **响应**：
  ```json
  {
    "path": "/webtv/mms/vod/?tenant/?year/?month/?day/?task_id",
    "valid": true,
    "variables": [
      "?tenant",
      "?year",
      "?month",
      "?day",
      "?task_id"
    ]
  }
  ```

#### 5.1.4 查询支持的变量
- **路径**：`/api/transcode/path/variables`
- **方法**：GET
- **响应**：
  ```json
  {
    "variables": [
      {
        "name": "?tenant",
        "description": "租户 ID",
        "example": "tenant_001",
        "type": "STRING"
      },
      {
        "name": "?year",
        "description": "当前年份（4 位）",
        "example": "2026",
        "type": "NUMBER"
      },
      {
        "name": "?month",
        "description": "当前月份（2 位，补 0）",
        "example": "02",
        "type": "NUMBER"
      },
      {
        "name": "?day",
        "description": "当前日期（2 位，补 0）",
        "example": "13",
        "type": "NUMBER"
      },
      {
        "name": "?task_id",
        "description": "任务 ID",
        "example": "task_123456",
        "type": "STRING"
      }
    ]
  }
  ```

## 6. 实现方案

### 6.1 技术选型
- **变量解析**：正则表达式
- **变量替换**：字符串替换
- **日期时间**：Java 8 Time API
- **UUID 生成**：Java UUID

### 6.2 核心组件
- **PathVariableResolver**：路径变量解析器
- **PathVariableReplacer**：路径变量替换器
- **PathVariableValidator**：路径变量验证器
- **PathVariableNormalizer**：路径规范化器
- **PathVariableRegistry**：路径变量注册表

### 6.3 关键流程

#### 6.3.1 变量解析流程
1. 接收原始路径
2. 使用正则表达式匹配变量
3. 提取所有变量
4. 验证变量的合法性
5. 返回变量列表

#### 6.3.2 变量替换流程
1. 接收原始路径和变量值
2. 解析路径中的变量
3. 获取变量的值
4. 替换路径中的变量
5. 规范化路径
6. 返回替换后的路径

#### 6.3.3 变量值获取流程
1. 检查变量类型
2. 根据变量类型获取值
3. 格式化变量值
4. 应用默认值（如果需要）
5. 返回变量值

#### 6.3.4 路径规范化流程
1. 统一路径分隔符
2. 去除连续的分隔符
3. 去除首尾的分隔符
4. 转义特殊字符
5. 验证路径长度
6. 返回规范化后的路径

## 7. 监控与告警

### 7.1 监控指标
- 变量替换次数
- 变量替换成功率
- 路径生成次数
- 路径验证次数

### 7.2 告警机制
- 变量替换失败率过高
- 路径验证失败率过高
- 路径冲突率过高

## 8. 部署与运维

### 8.1 依赖
- JDK 25+
- Spring Boot 3.x

### 8.2 配置
- 路径分隔符
- 路径最大长度
- 变量前缀
- 变量后缀

### 8.3 运维操作
- 查看支持的变量列表
- 查看变量替换统计
- 查看路径验证结果

## 9. 测试计划

### 9.1 单元测试
- 变量解析测试
- 变量替换测试
- 路径规范化测试
- 路径验证测试

### 9.2 集成测试
- 与转码引擎集成测试
- 与文件系统集成测试

### 9.3 性能测试
- 变量替换性能测试
- 路径生成性能测试
- 大量路径处理测试

### 9.4 边界测试
- 超长路径测试
- 特殊字符测试
- 无效变量测试
- 路径遍历测试

## 10. 风险评估

### 10.1 潜在风险
- **路径遍历攻击**：恶意构造路径导致路径遍历
- **变量注入**：恶意变量导致安全问题
- **路径冲突**：路径冲突导致文件覆盖
- **路径过长**：路径过长导致系统错误
- **变量替换失败**：变量替换失败导致转码失败

### 10.2 缓解措施
- **路径验证**：验证路径的合法性，防止路径遍历
- **变量验证**：验证变量的值，防止变量注入
- **冲突检测**：检测路径冲突，防止文件覆盖
- **路径长度限制**：限制路径长度，防止系统错误
- **默认值处理**：为变量设置默认值，防止替换失败