# FFmpeg 命令行迁移 — 技术设计

## 1. 功能与兼容性矩阵

| 功能 | 当前实现（JavaCV） | 迁移方案（FFmpeg/ffprobe CLI） | 兼容性要点 |
|------|-------------------|--------------------------------|------------|
| **转码 (transcode)** | FFmpegFrameGrabber + FFmpegFrameRecorder，逐帧 grab/record | `ffmpeg -i input -c:v libx264 ... -c:a aac output` | 分辨率(resolution)、码率(bitrate)、帧率(frameRate)、编码器(encoder)、格式(targetFormat)、movflags +faststart、像素格式 yuv420p |
| **转码进度回调** | grabber.getTimestamp() / getLengthInTime()，约 300ms 上报 | 解析 stderr 的 `time=...` 或使用 `-progress pipe:N` | 保持 reportStepProgress 语义与节流频率 |
| **转码后水印** | addWatermark 已用 ProcessBuilder ffmpeg overlay | 保持不变 | 无变更 |
| **抽帧 (extract_frames)** | 顺序 grab，按 frameInterval 取帧，写 jpg/png | `ffmpeg -vf "select=not(mod(n\,interval))" -vsync vfr frame_%d.jpg` 等 | frameInterval、extractFrameCount、单帧/多帧输出路径、extractOutputFormat、取消时终止进程 |
| **雪碧图 (sprite)** | FFmpegFrameFilter select+scale+tile 或先抽帧再 Java 拼图 | 视频：ffmpeg -vf "select,...,scale,...,tile=colsxrows" -frames:v 1；目录：保留 loadFramesFromDirectory + buildSpriteFromImages | spriteColumns、spriteRows、spriteScale、输出路径与扩展名 |
| **媒体探测 (probe)** | FFmpegFrameGrabber 读 width/height/frameRate/length/codec | `ffprobe -v quiet -print_format json -show_format -show_streams`，解析 JSON | ProbeResult 各字段一致；codec 可为 name 或 id，需约定映射 |

## 2. 架构

- **FFmpeg/ffprobe 调用层**：新增统一封装（如 `FFmpegCliExecutor`），负责：
  - 构建 ffmpeg/ffprobe 参数列表；
  - 启动进程、重定向 stdin/stderr、stdout（ffprobe json）；
  - 解析 stderr 或 -progress 支持进度回调；
  - 超时与取消（destroy 子进程）；
  - 错误时解析 stderr 抛出友好异常。
- **MediaStepOps**：保留 doTranscode、doExtractFrames、doSpriteSheet 等对外 API，内部改为调用上述 CLI 封装。
- **ProbeStepExecutor**：改为调用 ffprobe，解析 JSON 填充 ProbeResult。
- **工具方法**：toLocalFilePath、baseName、parentPath、ensureVideoExtension、ensureImageExtension 等保持不变。

## 3. 各步骤参数映射

### 3.1 转码 (doTranscode)

- resolution → `-s widthxheight`
- bitrate → `-b:v N`（单位 bps）
- frameRate → `-r N`
- encoder → `-c:v libx264` 或指定编码器
- targetFormat → `-f format` 与输出扩展名
- mp4 时 `-movflags +faststart`，`-pix_fmt yuv420p`，`-c:a aac` 等与现有一致
- 进度：`-progress pipe:1` 或解析 stderr `time=HH:MM:SS.xx`，换算 duration 百分比，节流约 300ms 调用 reportStepProgress
- 取消：context.isCancelled() 为 true 时 destroy 进程

### 3.2 抽帧 (doExtractFrames)

- 单帧：`-vf "select=eq(n\,0)"` 或按 interval 取第 N 帧，`-vframes 1`
- 多帧：`-vf "select=not(mod(n\,interval))" -vsync vfr`，输出 frame_%d.jpg/png 到指定目录
- frameInterval、extractFrameCount、extractOutputFormat 按 StrategyStepVO 映射；取消时终止进程

### 3.3 雪碧图 (doSpriteSheet)

- 输入为目录：沿用 loadFramesFromDirectory + buildSpriteFromImages（纯 Java）
- 输入为视频：`-vf "select=not(mod(n\,interval)),scale=iw/scale:ih/scale,tile=colsxrows" -frames:v 1`，interval 由时长与 cols*rows 计算

### 3.4 探测 (ProbeStepExecutor)

- 命令：`ffprobe -v quiet -print_format json -show_format -show_streams <input>`
- 从 streams 取视频流 width/height、codec_name、r_frame_rate、duration；从 format 取 duration/bit_rate；填充 ProbeResult；codec 与现有使用方约定（name 或 id 映射）

## 4. 依赖与部署

- 移除 javacv-platform。
- 运行环境需预装 ffmpeg、ffprobe，PATH 可用，版本建议 4.x+。
- 可选：启动或首次调用时执行 `ffmpeg -version` 做环境检测。
