# 从 JavaCV 迁移到 FFmpeg 命令行方案

## Why

当前 kitty-transcoder 的媒体处理基于 **JavaCV（Bytedeco FFmpeg 绑定）** 实现，存在以下问题：

1. **性能**：JavaCV 需在 JVM 内解码/编码并拷贝帧数据，开销大于直接调用 FFmpeg 进程；命令行方案可充分利用 FFmpeg 原生优化与多线程。
2. **兼容性**：JavaCV 自带 native 库与系统/容器环境可能不一致，导致格式、编码兼容问题；与系统安装的 FFmpeg 版本一致更易保障兼容性。
3. **文档与生态**：FFmpeg 官方文档、社区示例、问题排查均以命令行为主，便于维护与扩展。
4. **依赖**：`javacv-platform` 体积大、native 绑定复杂，移除后可显著减小依赖与构建复杂度。

因此需要将转码引擎从 JavaCV 迁移到 **FFmpeg / ffprobe 命令行**，在兼容现有功能的前提下提升性能与可维护性。

## What Changes

- **转码 (transcode)**：由 FFmpegFrameGrabber + FFmpegFrameRecorder 逐帧处理，改为调用 `ffmpeg -i input -c:v libx264 ... -c:a aac output` 等命令行；分辨率、码率、帧率、编码器、格式、movflags +faststart、像素格式等参数通过命令行参数传递；进度通过解析 ffmpeg stderr 或 `-progress pipe:N` 实现。
- **抽帧 (extract_frames)**：由 JavaCV 顺序 grab 转 BufferedImage 写文件，改为 `ffmpeg -vf "select=..."` 等命令输出 jpg/png；单帧与多帧、frameInterval/extractFrameCount/extractOutputFormat 行为与现有一致。
- **雪碧图 (sprite)**：视频输入由 FFmpegFrameFilter select+scale+tile 改为 ffmpeg `-vf "select,...,scale,...,tile=colsxrows" -frames:v 1`；目录输入仍保留现有 Java 拼图逻辑（loadFramesFromDirectory + buildSpriteFromImages），不依赖 JavaCV。
- **媒体探测 (probe)**：由 FFmpegFrameGrabber 读属性改为 `ffprobe -print_format json -show_format -show_streams`，解析 JSON 填充 ProbeResult。
- **水印**：已使用 ProcessBuilder 调用 ffmpeg overlay，保持不变。
- **图片转换 (image_convert)**：已使用 ImageMagick 命令行，保持不变。
- **依赖**：从 kitty-transcoder-func 移除 javacv-platform；运行环境需预装 ffmpeg/ffprobe（PATH 可用），版本建议 4.x+。

## Capabilities

### New Capabilities

- `ffmpeg-cli-executor`: 统一的 FFmpeg/ffprobe 进程封装，负责构建参数、启动进程、解析 stderr/-progress、超时与取消、错误信息解析；可配置可执行路径。
- `ffprobe-json-parser`: 将 ffprobe 的 JSON 输出解析为 ProbeResult，支持无视频/无音频/多流等边界情况。

### Modified Capabilities

- `ffmpeg-transcode-engine`: 由“JavaCV 逐帧 grab/record”改为“调用 ffmpeg 命令行”；对外 API（MediaStepOps.doTranscode / 各 StepExecutor）保持不变，内部改为调用 FFmpeg CLI；进度与取消语义不变。
- `transcode-step-probe`: ProbeStepExecutor 由 FFmpegFrameGrabber 改为 ffprobe JSON + 解析，输出 ProbeResult 字段与现有一致。
- `transcode-step-extract-frames`: doExtractFrames 由 JavaCV 抽帧写图改为 ffmpeg -vf select 输出；单帧/多帧路径与格式（jpg/png）兼容。
- `transcode-step-sprite`: 视频输入由 FFmpegFrameFilter 改为 ffmpeg -vf tile；目录输入保留现有 Java 拼图。

### Unchanged (Clarified)

- `transcode-watermark`: addWatermark 已为 ffmpeg 命令，接口与行为不变。
- `transcode-image-convert`: 已为 ImageMagick 命令，不变。
- 路径与工作目录逻辑（toLocalFilePath、baseName、parentPath、ensureVideoExtension、ensureImageExtension）不变。

## Impact

- **依赖**：
  - 移除：javacv-platform（及 bytedeco 相关）。
  - 约定：运行环境预装 ffmpeg、ffprobe，PATH 可用；推荐版本 4.x+，在 README/运维文档中说明；可选启动时 `ffmpeg -version` 检测。
- **配置**：
  - 可选：ffmpeg/ffprobe 可执行路径（默认从 PATH 解析）、进程超时时间等。
- **测试**：
  - 所有新增/改动核心逻辑必须具备充分的 JUnit5 单元测试；转码/抽帧/雪碧图/探测的 CLI 封装与解析均需单测覆盖，保障兼容性与回归。
- **兼容与回退**：
  - 迁移在独立分支进行；保留原 JavaCV 实现直至新实现通过全部测试与验收；必要时可通过配置开关回退（可选）。

## 非目标（Out of Scope）

- 本提案仅覆盖“用 FFmpeg/ffprobe 命令行替代 JavaCV 实现现有步骤”，不新增新的步骤类型或新的媒体格式支持。
- 多步骤 DAG 执行顺序、策略存储、任务队列等与当前一致，不因本次迁移而变更。

## 实施说明

- 详细技术设计与参数映射见 **design.md**。
- 可执行任务拆分为 **tasks.md**，按 Phase 顺序实施；每项均需配合 JUnit5 单元测试。
- **先做提案与设计评审，审查通过后再实现代码。**
