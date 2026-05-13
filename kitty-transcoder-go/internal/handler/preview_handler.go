package handler

import (
	"net/http"
	"os"
	"path/filepath"
	"strings"

	"kitty-transcoder-go/internal/repository"

	"github.com/gogf/gf/v2/frame/g"
	"github.com/gogf/gf/v2/net/ghttp"
)

type PreviewHandler struct {
	taskRepo *repository.TaskRepo
	workDir  string
}

func NewPreviewHandler(taskRepo *repository.TaskRepo, workDir string) *PreviewHandler {
	return &PreviewHandler{taskRepo: taskRepo, workDir: workDir}
}

func (h *PreviewHandler) Register(group *ghttp.RouterGroup) {
	group.GET("/preview", h.preview)
	group.GET("/preview/info", h.previewInfo)
}

var mimeTypes = map[string]string{
	"mp4":  "video/mp4",
	"m4v":  "video/x-m4v",
	"webm": "video/webm",
	"mkv":  "video/x-matroska",
	"avi":  "video/x-msvideo",
	"mov":  "video/quicktime",
	"jpg":  "image/jpeg",
	"jpeg": "image/jpeg",
	"png":  "image/png",
	"gif":  "image/gif",
	"webp": "image/webp",
}

func (h *PreviewHandler) preview(r *ghttp.Request) {
	taskId := r.Get("taskId").String()
	path := r.Get("path").String()

	if taskId == "" {
		r.Response.WriteStatus(400)
		r.Response.Header().Set("Content-Type", "application/json; charset=UTF-8")
		r.Response.WriteJson(g.Map{"error": "taskId is required"})
		return
	}

	task, err := h.taskRepo.GetByID(taskId)
	if err != nil || task == nil {
		r.Response.WriteStatus(404)
		r.Response.Header().Set("Content-Type", "application/json; charset=UTF-8")
		r.Response.WriteJson(g.Map{"error": "task not found"})
		return
	}

	var filePath string
	if path == "" {
		filePath = task.OutputPath
	} else {
		if task.StepOutputs != "" {
			filePath = h.resolveStepOutput(task.StepOutputs, path)
		}
		if filePath == "" {
			filePath = filepath.Join(filepath.Dir(task.OutputPath), path)
		}
	}

	if filePath == "" || !h.isSubPath(filePath) {
		r.Response.WriteStatus(404)
		r.Response.Header().Set("Content-Type", "application/json; charset=UTF-8")
		r.Response.WriteJson(g.Map{"error": "file not found"})
		return
	}

	file, err := os.Open(filePath)
	if err != nil {
		r.Response.WriteStatus(404)
		r.Response.Header().Set("Content-Type", "application/json; charset=UTF-8")
		r.Response.WriteJson(g.Map{"error": "file not found"})
		return
	}
	defer file.Close()

	stat, err := file.Stat()
	if err != nil {
		r.Response.WriteStatus(404)
		r.Response.Header().Set("Content-Type", "application/json; charset=UTF-8")
		r.Response.WriteJson(g.Map{"error": "file not found"})
		return
	}

	contentType := guessContentType(stat.Name())
	r.Response.Header().Set("Content-Type", contentType)
	r.Response.Header().Set("Content-Disposition", "inline; filename=\""+stat.Name()+"\"")
	r.Response.Header().Set("Accept-Ranges", "bytes")
	http.ServeContent(r.Response.ResponseWriter, r.Request, stat.Name(), stat.ModTime(), file)
}

func (h *PreviewHandler) previewInfo(r *ghttp.Request) {
	taskId := r.Get("taskId").String()
	baseUrl := r.Get("baseUrl").String()

	if taskId == "" {
		r.Response.WriteStatus(400)
		r.Response.Header().Set("Content-Type", "application/json; charset=UTF-8")
		r.Response.WriteJson(g.Map{"error": "taskId is required"})
		return
	}

	task, err := h.taskRepo.GetByID(taskId)
	if err != nil || task == nil {
		r.Response.WriteStatus(404)
		r.Response.Header().Set("Content-Type", "application/json; charset=UTF-8")
		r.Response.WriteJson(g.Map{"error": "task not found"})
		return
	}

	prefix := baseUrl
	if prefix != "" {
		prefix = strings.TrimSuffix(prefix, "/")
	}

	var files []map[string]string
	if task.OutputPath != "" && h.isSubPath(task.OutputPath) {
		previewUrl := "/api/transcode/preview?taskId=" + taskId
		if prefix != "" {
			previewUrl = prefix + previewUrl
		}
		files = append(files, map[string]string{
			"path":       task.OutputPath,
			"previewUrl": previewUrl,
		})
	}

	if task.StepOutputs != "" {
		stepFiles := h.listStepOutputs(task.StepOutputs, taskId, prefix)
		files = append(files, stepFiles...)
	}

	r.Response.Header().Set("Content-Type", "application/json; charset=UTF-8")
	r.Response.WriteJson(g.Map{
		"taskId": taskId,
		"files":  files,
	})
}

func (h *PreviewHandler) resolveStepOutput(stepOutputsJson, relativePath string) string {
	return ""
}

func (h *PreviewHandler) listStepOutputs(stepOutputsJson, taskId, prefix string) []map[string]string {
	var result []map[string]string
	parts := strings.Split(stepOutputsJson, ",")
	for _, part := range parts {
		part = strings.TrimSpace(part)
		if part == "" {
			continue
		}
		kv := strings.SplitN(part, ":", 2)
		if len(kv) == 2 {
			path := strings.Trim(kv[1], `"`)
			if path != "" && h.isSubPath(path) {
				previewUrl := "/api/transcode/preview?taskId=" + taskId + "&path=" + filepath.Base(path)
				if prefix != "" {
					previewUrl = prefix + previewUrl
				}
				result = append(result, map[string]string{
					"path":       path,
					"previewUrl": previewUrl,
				})
			}
		}
	}
	return result
}

func (h *PreviewHandler) isSubPath(path string) bool {
	if path == "" {
		return false
	}
	absPath, err := filepath.Abs(path)
	if err != nil {
		return false
	}
	absWorkDir, err := filepath.Abs(h.workDir)
	if err != nil {
		return true
	}
	// 统一使用正斜杠进行比较，避免 Windows 反斜杠问题
	absPath = strings.ReplaceAll(absPath, "\\", "/")
	absWorkDir = strings.ReplaceAll(absWorkDir, "\\", "/")
	return strings.HasPrefix(absPath, absWorkDir)
}

func guessContentType(filename string) string {
	ext := strings.ToLower(filepath.Ext(filename))
	ext = strings.TrimPrefix(ext, ".")
	if mime, ok := mimeTypes[ext]; ok {
		return mime
	}
	return "application/octet-stream"
}
