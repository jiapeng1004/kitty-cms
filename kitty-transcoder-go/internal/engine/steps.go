package engine

import (
	"context"
	"encoding/json"
	"fmt"
	"os"
	"path/filepath"
	"strings"

	"kitty-transcoder-go/internal/model"
)

// StepExecutor 步骤执行器接口
type StepExecutor interface {
	Type() string
	Execute(ctx context.Context, inputPath string, step *model.TranscodeStrategyStep, stepSuffix string, runCtx *StepContext) (outputPath string, err error)
}

// TranscodeStep 转码步骤：ffmpeg
type TranscodeStep struct{}

func (TranscodeStep) Type() string { return "transcode" }

func (s TranscodeStep) Execute(ctx context.Context, inputPath string, step *model.TranscodeStrategyStep, stepSuffix string, runCtx *StepContext) (string, error) {
	var p StepParam
	if step.Param != "" {
		_ = json.Unmarshal([]byte(step.Param), &p)
	}
	workDir := runCtx.WorkDir
	if workDir == "" {
		workDir = filepath.Dir(inputPath)
	}
	if workDir == "" {
		workDir = os.TempDir()
	}
	outPath := runCtx.ResolvedOutputPath
	if outPath == "" {
		base := strings.TrimSuffix(filepath.Base(inputPath), filepath.Ext(inputPath))
		// 与 Java 一致：输出基于 workDir，避免相对 inputPath 时写到错误目录
		outPath = filepath.Join(workDir, base+stepSuffix+"."+p.TargetFormat())
	}
	outPath = ensureVideoExt(outPath, p.TargetFormat())
	if err := os.MkdirAll(filepath.Dir(outPath), 0755); err != nil {
		return "", err
	}
	args := TranscodeArgs(inputPath, outPath, p.Resolution(), p.Bitrate(), p.FrameRate(), p.Encoder(), p.TargetFormat())
	durationSec := GetDurationSec(ctx, inputPath)
	var onProgress func(int)
	var cancelled func() bool
	if runCtx != nil {
		onProgress = func(percent int) {
			if runCtx.ReportProgress != nil {
				runCtx.ReportProgress(step.StepID, percent)
			}
		}
		if runCtx.Cancelled != nil {
			cancelled = runCtx.Cancelled
		}
	}
	_, _, err := RunFFmpegWithProgress(ctx, args, durationSec, onProgress, cancelled)
	if err != nil {
		return "", fmt.Errorf("ffmpeg transcode: %w", err)
	}
	return outPath, nil
}

// ExtractFramesStep 抽帧步骤：ffmpeg
type ExtractFramesStep struct{}

func (ExtractFramesStep) Type() string { return "extract_frames" }

func (s ExtractFramesStep) Execute(ctx context.Context, inputPath string, step *model.TranscodeStrategyStep, stepSuffix string, runCtx *StepContext) (string, error) {
	var p StepParam
	if step.Param != "" {
		_ = json.Unmarshal([]byte(step.Param), &p)
	}
	interval := p.FrameInterval()
	count := p.ExtractFrameCount()
	outFmt := p.ExtractOutputFormat()
	workDir := runCtx.WorkDir
	if workDir == "" {
		workDir = filepath.Dir(inputPath)
	}
	if workDir == "" {
		workDir = os.TempDir()
	}
	outPath := runCtx.ResolvedOutputPath
	if outPath == "" {
		base := strings.TrimSuffix(filepath.Base(inputPath), filepath.Ext(inputPath))
		// 与 Java 一致：输出基于 workDir，避免仅文件名时 filepath.Dir(inputPath)=.
		outPath = filepath.Join(workDir, base+stepSuffix)
	}
	if count == 1 {
		outPath = outPath + "_frame." + outFmt
	} else {
		if err := os.MkdirAll(outPath, 0755); err != nil {
			return "", err
		}
	}
	args := ExtractFramesArgs(inputPath, outPath, interval, count, outFmt)
	_, _, err := RunFFmpeg(ctx, args)
	if err != nil {
		return "", fmt.Errorf("ffmpeg extract_frames: %w", err)
	}
	return outPath, nil
}

// SpriteStep 雪碧图：ffmpeg filter_complex 或先抽帧再 magick montage
type SpriteStep struct{}

func (SpriteStep) Type() string { return "sprite" }

func (s SpriteStep) Execute(ctx context.Context, inputPath string, step *model.TranscodeStrategyStep, stepSuffix string, runCtx *StepContext) (string, error) {
	var p StepParam
	if step.Param != "" {
		_ = json.Unmarshal([]byte(step.Param), &p)
	}
	cols := p.SpriteColumns()
	rows := p.SpriteRows()
	scale := p.SpriteScale()
	count := cols * rows
	outFmt := p.ExtractOutputFormat()
	workDir := runCtx.WorkDir
	if workDir == "" {
		workDir = filepath.Dir(inputPath)
	}
	if workDir == "" {
		workDir = os.TempDir()
	}
	outPath := runCtx.ResolvedOutputPath
	if outPath == "" {
		base := strings.TrimSuffix(filepath.Base(inputPath), filepath.Ext(inputPath))
		outPath = filepath.Join(workDir, base+stepSuffix+"_sprite."+outFmt)
	}
	outPath = ensureImageExt(outPath, outFmt)
	if err := os.MkdirAll(filepath.Dir(outPath), 0755); err != nil {
		return "", err
	}
	filter := SpriteFilter(cols, rows, count, scale)
	args := []string{"-y", "-i", inputPath, "-vf", filter, "-frames:v", "1", outPath}
	_, _, err := RunFFmpeg(ctx, args)
	if err != nil {
		return "", fmt.Errorf("ffmpeg sprite: %w", err)
	}
	return outPath, nil
}

// ImageConvertStep 图片转换：magick
type ImageConvertStep struct{}

func (ImageConvertStep) Type() string { return "image_convert" }

func (s ImageConvertStep) Execute(ctx context.Context, inputPath string, step *model.TranscodeStrategyStep, stepSuffix string, runCtx *StepContext) (string, error) {
	var p StepParam
	if step.Param != "" {
		_ = json.Unmarshal([]byte(step.Param), &p)
	}
	targetFmt := p.ImageTargetFormat()
	quality := p.ImageQuality()
	resize := p.ImageResize
	workDir := runCtx.WorkDir
	if workDir == "" {
		workDir = filepath.Dir(inputPath)
	}
	if workDir == "" {
		workDir = os.TempDir()
	}
	outPath := runCtx.ResolvedOutputPath
	if outPath == "" {
		base := strings.TrimSuffix(filepath.Base(inputPath), filepath.Ext(inputPath))
		outPath = filepath.Join(workDir, base+stepSuffix+"."+targetFmt)
	}
	if err := os.MkdirAll(filepath.Dir(outPath), 0755); err != nil {
		return "", err
	}
	args := ImageConvertArgs(inputPath, outPath, targetFmt, quality, resize)
	_, _, err := RunMagick(ctx, args)
	if err != nil {
		return "", fmt.Errorf("magick image_convert: %w", err)
	}
	return outPath, nil
}

func ensureVideoExt(path, format string) string {
	ext := strings.ToLower(filepath.Ext(path))
	if ext == ".mp4" || ext == ".mkv" || ext == ".avi" || ext == ".webm" {
		return path
	}
	return strings.TrimSuffix(path, ext) + "." + format
}

func ensureImageExt(path, format string) string {
	ext := strings.ToLower(filepath.Ext(path))
	if ext == ".png" || ext == ".jpg" || ext == ".jpeg" || ext == ".webp" || ext == ".gif" {
		return path
	}
	return strings.TrimSuffix(path, ext) + "." + format
}
