package handler

import (
	"context"
	"fmt"
	"io"
	"net/http"
	"net/url"
	"os"
	"path/filepath"
	"strings"
	"time"

	"kitty-transcoder-go/internal/config"
	"kitty-transcoder-go/internal/engine"
	"kitty-transcoder-go/internal/model"
	"kitty-transcoder-go/internal/repository"
	"kitty-transcoder-go/internal/service"

	"github.com/gogf/gf/v2/frame/g"
	"github.com/gogf/gf/v2/net/ghttp"
	"github.com/google/uuid"
)

type MagicHandler struct {
	svc         *service.MagicService
	strategySvc *service.StrategyService
	taskRepo    *repository.TaskRepo
	cfg         *config.Transcoder
	workDir     string
}

func NewMagicHandler(svc *service.MagicService, strategySvc *service.StrategyService, taskRepo *repository.TaskRepo, cfg *config.Transcoder, workDir string) *MagicHandler {
	return &MagicHandler{svc: svc, strategySvc: strategySvc, taskRepo: taskRepo, cfg: cfg, workDir: workDir}
}

func (h *MagicHandler) Register(group *ghttp.RouterGroup) {
	group.POST("/magic/image-convert", h.imageConvert)
	group.POST("/magic/extract-frames", h.extractFrames)
	group.POST("/magic/transcode", h.transcode)
}

type ImageConvertReq struct {
	InputType    string `json:"inputType"`
	InputPath    string `json:"inputPath"`
	TargetFormat string `json:"targetFormat"`
	Quality      int    `json:"quality"`
	Resize       string `json:"resize"`
}

func (h *MagicHandler) imageConvert(r *ghttp.Request) {
	var req ImageConvertReq
	if err := r.Parse(&req); err != nil {
		r.Response.WriteStatus(400)
		r.Response.Header().Set("Content-Type", "application/json; charset=UTF-8")
		r.Response.WriteJson(g.Map{"error": err.Error()})
		return
	}
	if req.InputPath == "" {
		r.Response.WriteStatus(400)
		r.Response.Header().Set("Content-Type", "application/json; charset=UTF-8")
		r.Response.WriteJson(g.Map{"error": "inputPath is required"})
		return
	}
	inputType := req.InputType
	if inputType == "" {
		inputType = "DISK"
	}
	targetFormat := req.TargetFormat
	if targetFormat == "" {
		targetFormat = "webp"
	}
	quality := req.Quality
	if quality == 0 {
		quality = 85
	}
	localPath, err := h.resolveInput(req.InputPath, inputType)
	if err != nil {
		r.Response.WriteStatus(500)
		r.Response.Header().Set("Content-Type", "application/json; charset=UTF-8")
		r.Response.WriteJson(g.Map{"error": "failed to download input: " + err.Error()})
		return
	}
	taskID := strings.ReplaceAll(uuid.New().String(), "-", "")
	now := time.Now()
	task := &model.TranscodeTask{
		ID:        taskID,
		TaskType:  "MAGIC_IMAGE_CONVERT",
		InputType: inputType,
		InputPath: req.InputPath,
		Status:    "RUNNING",
		Progress:  0,
		CreatedAt: now,
	}
	if err := h.taskRepo.Create(task); err != nil {
		r.Response.WriteStatus(500)
		r.Response.Header().Set("Content-Type", "application/json; charset=UTF-8")
		r.Response.WriteJson(g.Map{"error": "failed to create task: " + err.Error()})
		return
	}
	outPath, err := h.doImageConvert(localPath, targetFormat, quality, req.Resize, h.workDir, taskID)
	if err != nil {
		task.Status = "FAILED"
		task.ErrorMessage = err.Error()
		h.taskRepo.Save(task)
		r.Response.WriteStatus(500)
		r.Response.Header().Set("Content-Type", "application/json; charset=UTF-8")
		r.Response.WriteJson(g.Map{"error": "image convert failed: " + err.Error()})
		return
	}
	task.OutputPath = outPath
	task.OutputHttpURL = h.buildOutputHttpUrl(taskID, outPath)
	task.Status = "COMPLETED"
	task.Progress = 100
	nowCompleted := time.Now()
	task.CompletedAt = &nowCompleted
	h.taskRepo.Save(task)
	rsp := map[string]interface{}{
		"id":            taskID,
		"status":        "COMPLETED",
		"progress":      100,
		"outputPath":    outPath,
		"outputHttpUrl": task.OutputHttpURL,
	}
	r.Response.Header().Set("Content-Type", "application/json; charset=UTF-8")
	r.Response.WriteJson(rsp)
}

func (h *MagicHandler) doImageConvert(inputPath, targetFormat string, quality int, resize, workDir, taskID string) (string, error) {
	base := strings.TrimSuffix(filepath.Base(inputPath), filepath.Ext(inputPath))
	outPath := filepath.Join(workDir, fmt.Sprintf("%s_%s.%s", base, taskID, targetFormat))
	if err := os.MkdirAll(filepath.Dir(outPath), 0755); err != nil {
		return "", err
	}
	args := engine.ImageConvertArgs(inputPath, outPath, targetFormat, quality, resize)
	_, _, err := engine.RunMagick(context.Background(), args)
	if err != nil {
		return "", fmt.Errorf("magick image_convert: %w", err)
	}
	return outPath, nil
}

type ExtractFramesReq struct {
	InputPath     string `json:"inputPath"`
	InputType     string `json:"inputType"`
	OutputFormat  string `json:"outputFormat"`
	FrameCount    int    `json:"frameCount"`
	FrameInterval int    `json:"frameInterval"`
}

func (h *MagicHandler) extractFrames(r *ghttp.Request) {
	r.Response.WriteStatus(501)
	r.Response.Header().Set("Content-Type", "application/json; charset=UTF-8")
	r.Response.WriteJson(g.Map{"error": "not implemented yet"})
}

type TranscodeReq struct {
	InputPath  string `json:"inputPath"`
	InputType  string `json:"inputType"`
	StrategyID int64  `json:"strategyId"`
	OutputPath string `json:"outputPath"`
}

func (h *MagicHandler) transcode(r *ghttp.Request) {
	r.Response.WriteStatus(501)
	r.Response.Header().Set("Content-Type", "application/json; charset=UTF-8")
	r.Response.WriteJson(g.Map{"error": "not implemented yet"})
}

func (h *MagicHandler) resolveInput(inputPath, inputType string) (string, error) {
	if strings.ToUpper(inputType) == "HTTP" || strings.HasPrefix(inputPath, "http://") || strings.HasPrefix(inputPath, "https://") {
		// Download file to temp
		resp, err := http.Get(inputPath)
		if err != nil {
			return "", err
		}
		defer resp.Body.Close()
		if resp.StatusCode != 200 {
			return "", fmt.Errorf("http status: %d", resp.StatusCode)
		}
		// Create temp file
		ext := filepath.Ext(inputPath)
		if ext == "" {
			ext = ".tmp"
		}
		tmpFile, err := os.CreateTemp("", "magick_*"+ext)
		if err != nil {
			return "", err
		}
		defer tmpFile.Close()
		if _, err := io.Copy(tmpFile, resp.Body); err != nil {
			return "", err
		}
		return tmpFile.Name(), nil
	}
	return inputPath, nil
}

func (h *MagicHandler) buildOutputHttpUrl(taskID, outputPath string) string {
	if outputPath == "" {
		return ""
	}
	path := "/api/transcode/preview?taskId=" + taskID
	prefix := ""
	if h.cfg != nil && h.cfg.Output.HttpPrefix != "" {
		prefix = strings.TrimSuffix(h.cfg.Output.HttpPrefix, "/")
	}
	if prefix == "" {
		return path
	}
	result, err := url.JoinPath(prefix, path)
	if err != nil {
		return prefix + path
	}
	return result
}
