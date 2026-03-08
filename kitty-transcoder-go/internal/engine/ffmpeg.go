package engine

import (
	"bufio"
	"bytes"
	"context"
	"fmt"
	"os/exec"
	"path/filepath"
	"regexp"
	"strconv"
	"strings"
	"time"
)

// RunFFmpeg 执行 ffmpeg，args 不含 "ffmpeg" 本身；若 ctx 取消则终止进程
func RunFFmpeg(ctx context.Context, args []string) (stdout, stderr string, err error) {
	cmd := exec.CommandContext(ctx, "ffmpeg", args...)
	var outBuf, errBuf bytes.Buffer
	cmd.Stdout = &outBuf
	cmd.Stderr = &errBuf
	err = cmd.Run()
	return outBuf.String(), errBuf.String(), err
}

// time= 行格式示例: time=00:01:23.45 或 time=00:00:05.12
var reFFmpegTime = regexp.MustCompile(`time=(\d+):(\d+):(\d+)\.(\d+)`)

// RunFFmpegWithProgress 执行 ffmpeg 并解析 stderr 的 time= 行驱动实时进度回调；onProgress(0-100)，cancelled 可空
func RunFFmpegWithProgress(ctx context.Context, args []string, totalDurationSec float64, onProgress func(percent int), cancelled func() bool) (stdout, stderr string, err error) {
	cmd := exec.CommandContext(ctx, "ffmpeg", args...)
	var outBuf bytes.Buffer
	cmd.Stdout = &outBuf
	stderrPipe, err := cmd.StderrPipe()
	if err != nil {
		return "", "", err
	}
	if err := cmd.Start(); err != nil {
		return "", "", err
	}
	var errBuf bytes.Buffer
	lastReport := time.Now()
	lastPercent := -1
	scanner := bufio.NewScanner(stderrPipe)
	scanner.Buffer(make([]byte, 64*1024), 1024*1024)
	for scanner.Scan() {
		line := scanner.Text()
		errBuf.WriteString(line)
		errBuf.WriteByte('\n')
		if cancelled != nil && cancelled() {
			_ = cmd.Process.Kill()
			_ = cmd.Wait()
			return outBuf.String(), errBuf.String(), context.Canceled
		}
		if totalDurationSec <= 0 {
			continue
		}
		matches := reFFmpegTime.FindStringSubmatch(line)
		if len(matches) != 5 {
			continue
		}
		h, _ := strconv.Atoi(matches[1])
		m, _ := strconv.Atoi(matches[2])
		s, _ := strconv.Atoi(matches[3])
		cs, _ := strconv.Atoi(matches[4])
		currentSec := float64(h*3600+m*60+s) + float64(cs)/100.0
		percent := int((currentSec / totalDurationSec) * 100)
		if percent > 100 {
			percent = 100
		}
		if percent != lastPercent && time.Since(lastReport) >= 300*time.Millisecond {
			lastPercent = percent
			lastReport = time.Now()
			if onProgress != nil {
				onProgress(percent)
			}
		}
	}
	waitErr := cmd.Wait()
	if waitErr != nil {
		return outBuf.String(), errBuf.String(), waitErr
	}
	if onProgress != nil && lastPercent < 100 {
		onProgress(100)
	}
	return outBuf.String(), errBuf.String(), nil
}

// GetDurationSec 用 ffprobe 获取媒体时长（秒），失败返回 0
func GetDurationSec(ctx context.Context, inputPath string) float64 {
	out, _, err := RunFFprobe(ctx, []string{"-v", "error", "-show_entries", "format=duration", "-of", "default=noprint_wrappers=1:nokey=1", inputPath})
	if err != nil {
		return 0
	}
	s := strings.TrimSpace(out)
	if s == "" {
		return 0
	}
	f, err := strconv.ParseFloat(s, 64)
	if err != nil {
		return 0
	}
	return f
}

// RunFFprobe 执行 ffprobe，常用 -v error -show_entries format=duration -of default=noprint_wrappers=1:nokey=1
func RunFFprobe(ctx context.Context, args []string) (stdout, stderr string, err error) {
	cmd := exec.CommandContext(ctx, "ffprobe", args...)
	var outBuf, errBuf bytes.Buffer
	cmd.Stdout = &outBuf
	cmd.Stderr = &errBuf
	err = cmd.Run()
	return outBuf.String(), errBuf.String(), err
}

// TranscodeArgs 根据步骤参数生成 ffmpeg 转码参数（不含 -i input）
func TranscodeArgs(inputPath, outputPath string, resolution string, bitrateKbps, frameRate int, encoder, format string) []string {
	width, height := 1920, 1080
	if resolution != "" {
		parts := strings.FieldsFunc(resolution, func(r rune) bool { return r == 'x' || r == 'X' || r == '×' })
		if len(parts) >= 2 {
			fmt.Sscanf(strings.TrimSpace(parts[0]), "%d", &width)
			fmt.Sscanf(strings.TrimSpace(parts[1]), "%d", &height)
		}
	}
	if bitrateKbps <= 0 {
		bitrateKbps = 5000
	}
	if frameRate <= 0 {
		frameRate = 30
	}
	if encoder == "" {
		encoder = "libx264"
	}
	if format == "" {
		format = "mp4"
	}
	// -y 覆盖，-i 输入，-vf scale，-c:v -b:v -r，-c:a aac，输出
	args := []string{"-y", "-i", inputPath}
	args = append(args, "-vf", fmt.Sprintf("scale=%d:%d:force_original_aspect_ratio=decrease,pad=%d:%d:(ow-iw)/2:(oh-ih)/2", width, height, width, height))
	args = append(args, "-c:v", encoder, "-b:v", fmt.Sprintf("%dk", bitrateKbps), "-r", fmt.Sprintf("%d", frameRate))
	args = append(args, "-c:a", "aac", "-b:a", "128k", "-ar", "44100")
	if format == "mp4" {
		args = append(args, "-movflags", "+faststart")
	}
	args = append(args, outputPath)
	return args
}

// WatermarkArgs 在已有视频上加水印，输入视频、水印图、输出；overlay 位置
func WatermarkArgs(videoPath, watermarkPath, outputPath, position string) []string {
	overlay := "W-w-10:H-h-10"
	switch strings.ToLower(position) {
	case "top-left":
		overlay = "10:10"
	case "top-right":
		overlay = "W-w-10:10"
	case "bottom-left":
		overlay = "10:H-h-10"
	default:
		overlay = "W-w-10:H-h-10"
	}
	return []string{"-y", "-i", videoPath, "-i", watermarkPath,
		"-filter_complex", "[1]scale=iw/4:-1[wm];[0][wm]overlay=" + overlay,
		"-c:a", "copy", outputPath}
}

// ExtractFramesArgs 抽帧：每 interval 帧取一帧，最多 maxFrames 张，输出到 outDir 或单文件
func ExtractFramesArgs(inputPath, outDirOrFile string, interval, maxFrames int, format string) []string {
	if format == "" {
		format = "jpg"
	}
	// 单帧：输出为文件；多帧：输出为 outDir/frame_%d.jpg
	isDir := !strings.Contains(filepath.Base(outDirOrFile), ".")
	selectExpr := fmt.Sprintf("not(mod(n\\,%d))", interval)
	if interval <= 0 {
		interval = 30
		selectExpr = "not(mod(n\\,30))"
	}
	if maxFrames <= 0 {
		maxFrames = 1
	}
	var outPattern string
	if isDir {
		outPattern = filepath.Join(outDirOrFile, "frame_%d."+format)
	} else {
		outPattern = outDirOrFile
	}
	// -vf "select=...", -vsync vfr -frame_pts 1，输出
	args := []string{"-y", "-i", inputPath, "-vf", "select='" + selectExpr + "',setpts=N/FRAME_RATE/TB",
		"-vsync", "vfr", "-frame_pts", "1", "-frames:v", fmt.Sprintf("%d", maxFrames), outPattern}
	return args
}

// SpriteFilter 雪碧图：从视频均匀取 count 帧，scale 缩小 1/scale，拼成 cols x rows
func SpriteFilter(cols, rows, count, scale int) string {
	if scale <= 0 {
		scale = 4
	}
	if cols <= 0 {
		cols = 4
	}
	if rows <= 0 {
		rows = 3
	}
	if count <= 0 {
		count = cols * rows
	}
	// select 均匀抽帧 + scale + tile
	interval := 1
	if count > 1 {
		interval = 99999 / count
		if interval < 1 {
			interval = 1
		}
	}
	return fmt.Sprintf("select='not(mod(n\\,%d))',scale=iw/%d:ih/%d,tile=%dx%d", interval, scale, scale, cols, rows)
}
