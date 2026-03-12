package engine

import (
	"bytes"
	"context"
	"fmt"
	"os/exec"
	"strconv"
	"strings"
)

// RunMagick 执行 magick 命令，args 不含 "magick"
func RunMagick(ctx context.Context, args []string) (stdout, stderr string, err error) {
	cmd := exec.CommandContext(ctx, "magick", args...)
	var outBuf, errBuf bytes.Buffer
	cmd.Stdout = &outBuf
	cmd.Stderr = &errBuf
	err = cmd.Run()
	return outBuf.String(), errBuf.String(), err
}

// ImageConvertArgs 单张图片转换：输入、输出、目标格式、质量、缩放(如 800x600)
func ImageConvertArgs(inputPath, outputPath, targetFormat string, quality int, resize string) []string {
	args := []string{inputPath}
	if resize != "" {
		args = append(args, "-resize", resize)
	}
	tf := strings.ToLower(targetFormat)
	if quality > 0 && (strings.Contains(tf, "jpg") || strings.Contains(tf, "jpeg") || strings.Contains(tf, "webp")) {
		args = append(args, "-quality", strconv.Itoa(quality))
	}
	args = append(args, outputPath)
	return args
}

// MontageArgs 雪碧图：多张图片拼成 cols x rows，输出到 outPath
func MontageArgs(inputPaths []string, outPath string, cols, rows int) []string {
	c, r := cols, rows
	if c <= 0 {
		c = 4
	}
	if r <= 0 {
		r = 3
	}
	args := []string{"montage"}
	args = append(args, inputPaths...)
	args = append(args, "-tile", fmt.Sprintf("%dx%d", c, r), "-geometry", "+0+0", outPath)
	return args
}
