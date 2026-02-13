# Transcode Watermark 规范

## 1. 概述

本规范定义了转码服务的水印功能，用于在转码过程中为视频添加水印。水印支持图片水印，包括位置、大小、透明度等配置，满足版权保护和品牌展示等需求。

## 2. 功能需求

### 2.1 水印类型
- **图片水印**：支持添加图片水印（PNG、JPG、TGA 等格式）
- **文字水印**：支持添加文字水印（可选功能）

### 2.2 水印配置
- **水印图片**：指定水印图片的路径或 URL
- **水印位置**：指定水印在视频中的位置（左上、右上、左下、右下、居中等）
- **水印大小**：指定水印的宽度和高度
- **水印透明度**：指定水印的透明度（0-100%）
- **水印偏移**：指定水印相对于基准位置的偏移量（X、Y）
- **水印时间**：指定水印显示的时间范围（开始时间、结束时间）

### 2.3 水印效果
- **固定水印**：水印在视频的整个播放过程中固定显示
- **滚动水印**：水印在视频播放过程中滚动显示
- **淡入淡出**：水印支持淡入淡出效果

### 2.4 水印管理
- **水印模板**：支持创建和管理水印模板
- **水印预览**：支持预览水印效果
- **水印上传**：支持上传水印图片
- **水印删除**：支持删除水印图片和模板

## 3. 非功能需求

### 3.1 性能
- 水印处理速度：> 30fps
- 水印图片加载时间：< 1s
- 水印处理延迟：< 100ms

### 3.2 可靠性
- 水印添加成功率：> 99%
- 水印图片加载成功率：> 99%

### 3.3 可扩展性
- 支持新增水印类型
- 支持新增水印效果
- 支持自定义水印位置

### 3.4 安全性
- 水印图片访问控制
- 防止水印图片泄露
- 水印配置权限控制

## 4. 数据模型

### 4.1 数据库表

#### 4.1.1 transcode_watermark 表
| 字段名 | 数据类型 | 约束 | 描述 |
|-------|---------|------|------|
| id | VARCHAR(36) | PRIMARY KEY | 水印ID |
| name | VARCHAR(100) | NOT NULL | 水印名称 |
| type | VARCHAR(20) | NOT NULL | 水印类型（IMAGE、TEXT） |
| image_url | VARCHAR(500) | NULL | 水印图片URL |
| image_path | VARCHAR(500) | NULL | 水印图片路径 |
| width | INT | NOT NULL | 水印宽度 |
| height | INT | NOT NULL | 水印高度 |
| position | VARCHAR(20) | NOT NULL | 水印位置（TOP_LEFT、TOP_RIGHT、BOTTOM_LEFT、BOTTOM_RIGHT、CENTER） |
| offset_x | INT | NOT NULL DEFAULT 0 | X轴偏移 |
| offset_y | INT | NOT NULL DEFAULT 0 | Y轴偏移 |
| opacity | INT | NOT NULL DEFAULT 100 | 透明度（0-100） |
| start_time | INT | NULL | 开始时间（秒） |
| end_time | INT | NULL | 结束时间（秒） |
| effect | VARCHAR(20) | NOT NULL DEFAULT 'FIXED' | 水印效果（FIXED、SCROLL、FADE） |
| created_at | DATETIME | NOT NULL | 创建时间 |
| creator | VARCHAR(50) | NOT NULL | 创建者 |
| updated_at | DATETIME | NULL | 更新时间 |
| updater | VARCHAR(50) | NULL | 更新者 |

### 4.2 Redis 数据结构

#### 4.2.1 水印缓存
- **名称**：`transcode:watermark:{watermarkId}`
- **类型**：Redis Hash
- **结构**：存储水印配置信息

## 5. API 接口

### 5.1 水印管理接口

#### 5.1.1 创建水印
- **路径**：`/api/transcode/watermark`
- **方法**：POST
- **请求体**：
  ```json
  {
    "name": "logo",
    "type": "IMAGE",
    "imageUrl": "http://example.com/logo.png",
    "width": 100,
    "height": 100,
    "position": "TOP_RIGHT",
    "offsetX": 10,
    "offsetY": 10,
    "opacity": 80,
    "startTime": 0,
    "endTime": null,
    "effect": "FIXED"
  }
  ```
- **响应**：
  ```json
  {
    "watermarkId": "string",
    "name": "logo",
    "status": "CREATED",
    "createdAt": "timestamp"
  }
  ```

#### 5.1.2 查询水印列表
- **路径**：`/api/transcode/watermark`
- **方法**：GET
- **参数**：
  - `type`：水印类型（可选）
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
        "name": "logo",
        "type": "IMAGE",
        "imageUrl": "http://example.com/logo.png",
        "width": 100,
        "height": 100,
        "position": "TOP_RIGHT",
        "offsetX": 10,
        "offsetY": 10,
        "opacity": 80,
        "effect": "FIXED",
        "createdAt": "timestamp"
      }
    ]
  }
  ```

#### 5.1.3 查询水印详情
- **路径**：`/api/transcode/watermark/{id}`
- **方法**：GET
- **响应**：
  ```json
  {
    "id": "string",
    "name": "logo",
    "type": "IMAGE",
    "imageUrl": "http://example.com/logo.png",
    "imagePath": "/path/to/logo.png",
    "width": 100,
    "height": 100,
    "position": "TOP_RIGHT",
    "offsetX": 10,
    "offsetY": 10,
    "opacity": 80,
    "startTime": 0,
    "endTime": null,
    "effect": "FIXED",
    "createdAt": "timestamp",
    "updatedAt": "timestamp"
  }
  ```

#### 5.1.4 更新水印
- **路径**：`/api/transcode/watermark/{id}`
- **方法**：PUT
- **请求体**：
  ```json
  {
    "name": "logo",
    "width": 120,
    "height": 120,
    "opacity": 90
  }
  ```
- **响应**：
  ```json
  {
    "watermarkId": "string",
    "status": "UPDATED",
    "updatedAt": "timestamp"
  }
  ```

#### 5.1.5 删除水印
- **路径**：`/api/transcode/watermark/{id}`
- **方法**：DELETE
- **响应**：
  ```json
  {
    "watermarkId": "string",
    "status": "DELETED",
    "deletedAt": "timestamp"
  }
  ```

#### 5.1.6 上传水印图片
- **路径**：`/api/transcode/watermark/upload`
- **方法**：POST
- **Content-Type**：multipart/form-data
- **请求体**：
  ```
  file: [水印图片文件]
  ```
- **响应**：
  ```json
  {
    "imageUrl": "http://example.com/watermarks/logo.png",
    "imagePath": "/path/to/watermarks/logo.png",
    "fileSize": 10240,
    "uploadedAt": "timestamp"
  }
  ```

#### 5.1.7 预览水印
- **路径**：`/api/transcode/watermark/preview`
- **方法**：POST
- **请求体**：
  ```json
  {
    "videoUrl": "http://example.com/video.mp4",
    "watermarkId": "string",
    "duration": 10
  }
  ```
- **响应**：
  ```json
  {
    "previewUrl": "http://example.com/previews/preview.mp4",
    "createdAt": "timestamp"
  }
  ```

## 6. 实现方案

### 6.1 技术选型
- **FFmpeg 水印**：使用 FFmpeg 的 overlay 滤镜
- **图片处理**：JavaCV
- **文件存储**：本地文件系统或对象存储

### 6.2 核心组件
- **WatermarkProcessor**：水印处理器
- **WatermarkManager**：水印管理器
- **WatermarkUploader**：水印上传器
- **WatermarkPreview**：水印预览器
- **WatermarkCache**：水印缓存

### 6.3 关键流程

#### 6.3.1 水印添加流程
1. 获取水印配置
2. 加载水印图片
3. 构建 FFmpeg 命令
4. 执行转码并添加水印
5. 验证输出文件
6. 清理临时文件

#### 6.3.2 水印参数构建流程
1. 解析水印配置
2. 计算水印位置
3. 构建水印参数
4. 构建 FFmpeg overlay 滤镜
5. 生成转码命令

#### 6.3.3 水印上传流程
1. 接收上传的水印图片
2. 验证图片格式和大小
3. 保存图片到存储
4. 生成图片 URL
5. 返回上传结果

#### 6.3.4 水印预览流程
1. 接收预览请求
2. 获取水印配置
3. 获取视频片段
4. 添加水印到视频片段
5. 生成预览视频
6. 返回预览 URL

## 7. 监控与告警

### 7.1 监控指标
- 水印添加成功率
- 水印图片加载时间
- 水印处理延迟
- 水印使用统计

### 7.2 告警机制
- 水印添加失败率过高
- 水印图片加载失败
- 水印处理延迟过高

## 8. 部署与运维

### 8.1 依赖
- FFmpeg
- JavaCV
- JDK 25+
- Spring Boot 3.x

### 8.2 配置
- 水印图片存储路径
- 水印图片 URL 前缀
- 水印图片最大大小
- 水印图片允许格式
- 预览视频时长

### 8.3 运维操作
- 查看水印列表
- 查看水印使用统计
- 清理未使用的水印图片
- 查看水印处理日志

## 9. 测试计划

### 9.1 单元测试
- 水印处理器测试
- 水印参数构建测试
- 水印上传测试
- 水印预览测试

### 9.2 集成测试
- FFmpeg 水印集成测试
- 文件存储集成测试
- 转码引擎集成测试

### 9.3 性能测试
- 水印处理性能测试
- 并发水印处理测试
- 水印图片加载性能测试

### 9.4 边界测试
- 大水印图片测试
- 不支持的图片格式测试
- 水印位置边界测试
- 水印透明度边界测试

## 10. 风险评估

### 10.1 潜在风险
- **水印图片丢失**：水印图片丢失导致无法添加水印
- **水印配置错误**：水印配置错误导致转码失败
- **水印性能影响**：水印处理影响转码性能
- **水印版权问题**：水印图片存在版权问题
- **水印安全问题**：水印图片泄露或被篡改

### 10.2 缓解措施
- **水印备份**：备份水印图片，防止丢失
- **配置验证**：验证水印配置，防止错误
- **性能优化**：优化水印处理，减少性能影响
- **版权审查**：审查水印图片版权，确保合法
- **访问控制**：控制水印图片访问，防止泄露