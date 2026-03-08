# kitty-transcoder-go

Go 版转码服务，与 [kitty-transcoder](../kitty-transcoder)（Java）功能对齐：策略化转码、抽帧、雪碧图、图片转换，使用 **GORM**、**GoFrame**、**ffmpeg** 与 **magick**（ImageMagick）命令行。

## 依赖

- **Go** 1.21+
- **ffmpeg**（含 ffprobe）已安装并加入 PATH
- **ImageMagick**（`magick` 命令）已安装并加入 PATH
- **数据库**：默认支持 **MySQL** 与 **SQLite**。不配置或 `database.default.link` 留空时自动使用 SQLite（`file:transcoder.db`）；配置 MySQL DSN 时使用 MySQL。
- **Redis**（可选）：用于**分布式任务取消**（发布订阅）。不配置时仅本机取消有效；多节点部署时配置 `redis.addr` 即可跨节点取消。

## 配置

复制并编辑 `config.yaml`：

- `server.address`: HTTP 服务端口（默认 `:8199`）
- `grpc.address`: gRPC 服务端口（默认 `:9090`）
- `database.default.link`: MySQL DSN；**留空或长度过短时自动使用 SQLite**（`file:transcoder.db`）
- `redis.addr`: Redis 地址（如 `localhost:6379`），**留空则禁用 Redis**，仅本机取消
- `transcoder.workDir`: 转码输出根目录
- `transcoder.output.httpPrefix`: 输出文件可访问的 HTTP 前缀（回调中的 outputHttpUrl）

## 构建与运行

```bash
cd kitty-transcoder-go
go mod tidy
go build -o kitty-transcoder-go.exe .
./kitty-transcoder-go.exe
```

- HTTP 默认 `:8199`，Swagger: `http://localhost:8199/swagger`
- **gRPC** 默认 `:9090`，与 Java 版 `TranscodeService` 对齐（CreateTask / GetTask / ListTasks / CancelTask / GetProgress / GetStrategies / GetStrategy / CreateStrategy / DeleteStrategy）

## API 概览（与 Java 版一致）

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/transcode/task | 创建转码任务 |
| GET | /api/transcode/task/{id} | 任务详情 |
| GET | /api/transcode/tasks | 分页任务列表 |
| DELETE | /api/transcode/task/{id} | 取消任务 |
| GET | /api/transcode/progress/{id} | 任务进度 |
| GET | /api/transcode/sse/{id} | 任务进度 SSE |
| POST | /api/transcode/strategy | 创建策略 |
| GET | /api/transcode/strategy | 策略列表 |
| GET | /api/transcode/strategy/{id} | 策略详情 |
| PUT | /api/transcode/strategy/{id} | 更新策略 |
| DELETE | /api/transcode/strategy/{id} | 删除策略 |

## 策略步骤类型

- **transcode**: 视频转码（分辨率、码率、编码器、可选水印）
- **extract_frames**: 按间隔或数量抽帧（jpg/png）
- **sprite**: 雪碧图（列数、行数、缩放）
- **image_convert**: 图片格式转换/缩放/质量（magick）

## 数据表

- `transcode_task`: 任务表（与 Java 版字段兼容）
- `transcode_strategy_step`: 策略步骤表（root_id + step_id 唯一）

首次运行会通过 GORM AutoMigrate 创建表结构。

## 分布式任务取消（Redis）

- 取消任务时写入 Redis 键 `transcoder:cancel:{taskId}` 并发布到 channel `transcoder:cancel:channel`。
- 执行节点订阅该 channel，收到后写入本地“已取消”集合；执行循环中仅查本地集合，避免热路径访问 Redis。
- 未配置 Redis 时，仅本机内存标记，单节点取消正常、多节点无法跨节点取消。
