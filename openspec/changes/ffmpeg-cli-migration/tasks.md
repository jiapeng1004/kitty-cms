# FFmpeg 命令行迁移 — 任务清单

按 Phase 顺序实施，每项需配合 JUnit5 单元测试。审查通过后再实现代码。

---

## Phase 1：基础设施

- [ ] 1.1 新增 FFmpeg/ffprobe 进程封装类：ProcessBuilder、工作目录、超时、取消、stderr 捕获与解析；可配置可执行路径
- [ ] 1.2 实现 ffprobe JSON 解析工具：输入 ffprobe -show_format -show_streams 的 JSON，输出 ProbeResult；覆盖无视频、无音频、多流等边界
- [ ] 1.3 单元测试：进程封装（参数传递、超时、取消）、ffprobe JSON 解析（各类流与缺失字段）

---

## Phase 2：探测 (Probe)

- [ ] 2.1 ProbeStepExecutor 改为调用 ffprobe，使用 JSON 解析填充 ProbeResult，不再使用 FFmpegFrameGrabber
- [ ] 2.2 单元测试：ProbeStepExecutor（文件/目录输入，ProbeResult 各字段断言）

---

## Phase 3：抽帧 (extract_frames)

- [ ] 3.1 doExtractFrames 改为 ffmpeg 命令：单帧与多帧、frameInterval、extractFrameCount、extractOutputFormat（jpg/png）；支持取消
- [ ] 3.2 单元测试：doExtractFrames（单帧/多帧、格式、输出路径、取消时进程终止）

---

## Phase 4：雪碧图 (sprite)

- [ ] 4.1 视频输入：雪碧图改为 ffmpeg -vf select+scale+tile -frames:v 1，替代 buildSpriteWithFilter（FFmpegFrameFilter）
- [ ] 4.2 目录输入：保留 loadFramesFromDirectory + buildSpriteFromImages，不依赖 JavaCV
- [ ] 4.3 单元测试：doSpriteSheet（视频输入、目录输入，输出文件与尺寸符合策略参数）

---

## Phase 5：转码 (transcode)

- [ ] 5.1 doTranscode 改为 ffmpeg 命令：分辨率、码率、帧率、编码器、格式、movflags、像素格式、音频 aac，与现有一致
- [ ] 5.2 转码进度：解析 ffmpeg 进度（stderr 或 -progress），调用 reportStepProgress，节流约 300ms
- [ ] 5.3 转码取消：context.isCancelled() 为 true 时终止 ffmpeg 进程
- [ ] 5.4 转码后水印：保持现有 addWatermark（ffmpeg 命令）逻辑不变
- [ ] 5.5 单元测试：doTranscode（多种 resolution/bitrate/format、进度回调被调用、取消时进程结束、有水印时输出正确）

---

## Phase 6：集成与清理

- [ ] 6.1 确认 MagicServiceImpl 仅依赖 MediaStepOps 与路径逻辑，无直接 JavaCV 依赖；若有则移除
- [ ] 6.2 从 kitty-transcoder-func pom.xml 移除 javacv-platform，删除所有 JavaCV import 与相关 native 配置
- [ ] 6.3 集成/回归：使用“2 转码 + 抽帧 + 雪碧图”等策略做端到端测试，对比迁移前后主输出与步骤输出路径、格式、可播性
- [ ] 6.4 文档：README 或运维文档注明需预装 ffmpeg/ffprobe、推荐版本；可选启动时版本检测说明
