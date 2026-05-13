package service

import (
	"context"
	"encoding/json"
	"errors"
	"fmt"
	"net/url"
	"os"
	"path/filepath"
	"sort"
	"strconv"
	"strings"
	"time"

	"kitty-transcoder-go/internal/broadcast"
	"kitty-transcoder-go/internal/cancel"
	"kitty-transcoder-go/internal/config"
	"kitty-transcoder-go/internal/db"
	"kitty-transcoder-go/internal/engine"
	"kitty-transcoder-go/internal/model"
	"kitty-transcoder-go/internal/repository"

	"github.com/google/uuid"
	"gorm.io/gorm"
)

type TaskService struct {
	taskRepo            *repository.TaskRepo
	strategyRepo        *repository.StrategyRepo
	engine              *engine.Engine
	cfg                 *config.Transcoder
	workDir             string
	db                  *gorm.DB
	cancelRegistry      *cancel.Registry
	progressBroadcaster *broadcast.ProgressBroadcaster
}

func NewTaskService(db *gorm.DB, taskRepo *repository.TaskRepo, strategyRepo *repository.StrategyRepo, eng *engine.Engine, cfg *config.Transcoder, workDir string, cancelRegistry *cancel.Registry, progressBroadcaster *broadcast.ProgressBroadcaster) *TaskService {
	if cancelRegistry == nil {
		cancelRegistry = cancel.NewRegistry(nil)
	}
	return &TaskService{taskRepo: taskRepo, strategyRepo: strategyRepo, engine: eng, cfg: cfg, workDir: workDir, db: db, cancelRegistry: cancelRegistry, progressBroadcaster: progressBroadcaster}
}

// GetProgressVO 返回 ProgressVO 用于 SSE/回调，与 Java getProgress 对齐
func (s *TaskService) GetProgressVO(taskID string) (*model.ProgressVO, error) {
	task, err := s.taskRepo.GetByID(taskID)
	if err != nil || task == nil {
		return nil, err
	}
	vo := &model.ProgressVO{
		TaskID:   task.ID,
		Progress: task.Progress,
		Status:   task.Status,
	}
	if task.StartedAt != nil {
		vo.ProcessingTime = int(time.Since(*task.StartedAt).Seconds())
	}
	return vo, nil
}

func (s *TaskService) CreateTask(req *model.CreateTaskReq) (*model.TranscodeTask, error) {
	id := strings.ReplaceAll(uuid.New().String(), "-", "")
	t := &model.TranscodeTask{
		ID:        id,
		TaskType:  "SCHEDULED_TRANSCODE",
		InputType: req.InputType,
		InputPath: req.InputPath,
		Status:    "PENDING",
		Progress:  0,
		Priority:  req.Priority,
		CreatedAt: time.Now(),
	}
	if req.StrategyID != 0 {
		t.StrategyID = &req.StrategyID
	}
	if req.WatermarkURL != "" {
		t.WatermarkURL = req.WatermarkURL
		t.WatermarkPosition = req.WatermarkPosition
	}
	if req.NotificationConfig != "" {
		t.NotificationConfig = req.NotificationConfig
	}
	if t.Priority == 0 {
		t.Priority = 5
	}
	if err := s.taskRepo.Create(t); err != nil {
		return nil, err
	}
	return t, nil
}

func (s *TaskService) GetTask(id string) (*model.TranscodeTask, error) {
	return s.taskRepo.GetByID(id)
}

// TaskToVO 将实体转为 TaskVO，与 Java TaskVO 对齐
func (s *TaskService) TaskToVO(t *model.TranscodeTask) *model.TaskVO {
	if t == nil {
		return nil
	}
	outputHttpURL := t.OutputHttpURL
	if outputHttpURL == "" && t.OutputPath != "" {
		outputHttpURL = s.buildOutputHttpURL(t.OutputPath)
	}
	vo := &model.TaskVO{
		ID:                t.ID,
		TaskType:          t.TaskType,
		Status:            t.Status,
		Progress:          t.Progress,
		InputType:         t.InputType,
		InputPath:         t.InputPath,
		InputFile:         t.InputPath,
		OutputPath:        t.OutputPath,
		OutputFile:        t.OutputPath,
		OutputHttpURL:     outputHttpURL,
		WatermarkURL:      t.WatermarkURL,
		WatermarkPosition: t.WatermarkPosition,
		ErrorMessage:      t.ErrorMessage,
	}
	if t.StrategyID != nil {
		vo.StrategyID = *t.StrategyID
	}
	vo.CreatedAt = t.CreatedAt.UnixMilli()
	if t.StartedAt != nil {
		vo.StartedAt = t.StartedAt.UnixMilli()
	}
	if t.CompletedAt != nil {
		vo.CompletedAt = t.CompletedAt.UnixMilli()
	}
	if t.NotificationConfig != "" {
		_ = json.Unmarshal([]byte(t.NotificationConfig), &vo.Notifications)
		// 兼容 DB 里存的是 type：供前端用的 method 与 Java 一致
		for i := range vo.Notifications {
			if vo.Notifications[i].Method == "" && vo.Notifications[i].Type != "" {
				vo.Notifications[i].Method = vo.Notifications[i].Type
			}
		}
	}
	return vo
}

// GetTaskVO 返回 TaskVO，与 Java getTask 对齐
func (s *TaskService) GetTaskVO(id string) (*model.TaskVO, error) {
	t, err := s.taskRepo.GetByID(id)
	if err != nil {
		return nil, err
	}
	return s.TaskToVO(t), nil
}

// ListTasksWithPagination 分页列表并返回分页信息，与 Java 对齐
func (s *TaskService) ListTasksWithPagination(page, pageSize int, status, taskType string) (*model.ListTasksResponse, error) {
	if page <= 0 {
		page = 1
	}
	if pageSize <= 0 {
		pageSize = 20
	}
	list, total, err := s.taskRepo.List(page, pageSize, status, taskType)
	if err != nil {
		return nil, err
	}
	vos := make([]model.TaskVO, 0, len(list))
	for i := range list {
		vos = append(vos, *s.TaskToVO(&list[i]))
	}
	return &model.ListTasksResponse{
		List:     vos,
		Total:    total,
		Page:     page,
		PageSize: pageSize,
	}, nil
}

func (s *TaskService) ListTasks(page, pageSize int, status, taskType string) ([]model.TranscodeTask, int64, error) {
	return s.taskRepo.List(page, pageSize, status, taskType)
}

func (s *TaskService) CancelTask(id string) error {
	s.cancelRegistry.MarkCancelled(id)
	return s.taskRepo.Cancel(id)
}

// DeleteTask 物理删除任务记录，与 Java deleteTask 对齐（先标记取消再删库）
func (s *TaskService) DeleteTask(id string) (bool, error) {
	s.cancelRegistry.MarkCancelled(id)
	return s.taskRepo.DeleteByID(id)
}

func (s *TaskService) ProcessTask(ctx context.Context, taskID string) error {
	task, err := s.taskRepo.GetByID(taskID)
	if err != nil || task == nil {
		return err
	}
	if task.Status == "CANCELLED" {
		return nil
	}
	s.cancelRegistry.RegisterRunning(taskID)
	defer s.cancelRegistry.Clear(taskID)
	now := time.Now()
	if err := db.WithTx(ctx, s.db, func(tx *gorm.DB) error {
		return tx.Model(&model.TranscodeTask{}).Where("id = ?", taskID).Updates(map[string]interface{}{
			"status": "PROCESSING", "started_at": now, "progress": 0,
		}).Error
	}); err != nil {
		return err
	}
	inputPath := task.InputPath
	if task.StrategyID == nil || *task.StrategyID == 0 {
		return fmt.Errorf("task has no strategy_id")
	}
	// 与 Java 一致：优先使用策略 workDir，否则全局 workDir
	effectiveWorkDir := s.cfg.WorkDir
	if effectiveWorkDir == "" {
		effectiveWorkDir = os.TempDir()
	}
	if first, _ := s.strategyRepo.GetFirstByRootID(*task.StrategyID); first != nil && first.WorkDir != "" {
		effectiveWorkDir = first.WorkDir
	}
	// 与 Java toLocalFilePath 一致：相对路径基于 workDir 转为绝对路径，避免 FFmpeg 找不到文件
	if inputPath != "" && !filepath.IsAbs(inputPath) {
		inputPath = filepath.Join(effectiveWorkDir, inputPath)
		inputPath = filepath.Clean(inputPath)
	}
	cancelled := func() bool { return s.cancelRegistry.IsCancelled(taskID) }
	reportProgress := func(stepID int, percent int) {
		_ = s.taskRepo.UpdateStatus(taskID, "PROCESSING", percent, "")
		if s.progressBroadcaster != nil {
			if vo, _ := s.GetProgressVO(taskID); vo != nil {
				s.progressBroadcaster.Broadcast(*vo)
			}
		}
	}
	result, err := s.engine.Transcode(ctx, taskID, inputPath, *task.StrategyID, effectiveWorkDir, reportProgress, cancelled)
	if err != nil {
		if errors.Is(err, context.Canceled) || (err != nil && strings.Contains(err.Error(), "cancel")) {
			_ = s.taskRepo.Cancel(taskID)
			if s.progressBroadcaster != nil {
				if vo, _ := s.GetProgressVO(taskID); vo != nil {
					s.progressBroadcaster.Broadcast(*vo)
				}
			}
			return nil
		}
		_ = s.taskRepo.UpdateError(taskID, err.Error())
		if s.progressBroadcaster != nil {
			if vo, _ := s.GetProgressVO(taskID); vo != nil {
				s.progressBroadcaster.Broadcast(*vo)
			}
		}
		return err
	}
	completedAt := time.Now()
	outputPath := result.MainOutput
	outputHttpURL := s.buildOutputHttpURL(outputPath)
	stepOutputsJSON := engine.StepOutputsJSON(result.StepOutputs)
	err = db.WithTx(ctx, s.db, func(tx *gorm.DB) error {
		return tx.Model(&model.TranscodeTask{}).Where("id = ?", taskID).Updates(map[string]interface{}{
			"status": "COMPLETED", "progress": 100,
			"output_path": outputPath, "output_http_url": outputHttpURL,
			"step_outputs": stepOutputsJSON, "completed_at": completedAt, "error_message": "",
		}).Error
	})
	if err == nil && s.progressBroadcaster != nil {
		if vo, _ := s.GetProgressVO(taskID); vo != nil {
			s.progressBroadcaster.Broadcast(*vo)
		}
	}
	return err
}

func (s *TaskService) buildOutputHttpURL(outputPath string) string {
	if s.cfg == nil || s.cfg.Output.HttpPrefix == "" || outputPath == "" {
		return ""
	}
	normalized := filepath.ToSlash(outputPath)
	if s.workDir != "" {
		workSlash := filepath.ToSlash(s.workDir)
		if strings.HasPrefix(normalized, workSlash+"/") {
			normalized = strings.TrimPrefix(normalized, workSlash+"/")
		} else if normalized == workSlash {
			normalized = ""
		}
	}
	if normalized == "" {
		normalized = "/"
	}
	httpPrefix := strings.TrimSuffix(s.cfg.Output.HttpPrefix, "/")
	result, err := url.JoinPath(httpPrefix, normalized)
	if err != nil {
		return httpPrefix + "/" + normalized
	}
	return result
}

// BuildStepOutputsList 用于回调：根据 step_outputs JSON 构建 StepOutputItem 列表
func (s *TaskService) BuildStepOutputsList(stepOutputsJSON string, strategyID int64) []model.StepOutputItem {
	if stepOutputsJSON == "" {
		return nil
	}
	var m map[string]string
	if err := json.Unmarshal([]byte(stepOutputsJSON), &m); err != nil {
		return nil
	}
	stepTypeByID := make(map[int]string)
	if strategyID > 0 {
		steps, _ := s.strategyRepo.GetByRootID(strategyID)
		for _, st := range steps {
			stepTypeByID[st.StepID] = st.Type
		}
	}
	keys := make([]int, 0, len(m))
	for k := range m {
		id, _ := strconv.Atoi(k)
		keys = append(keys, id)
	}
	sort.Ints(keys)
	list := make([]model.StepOutputItem, 0, len(keys))
	for _, stepID := range keys {
		k := strconv.Itoa(stepID)
		path := m[k]
		stype := stepTypeByID[stepID]
		if stype == "" {
			stype = "transcode"
		}
		item := model.StepOutputItem{StepID: stepID, StepType: stype, OutputPath: path}
		item.OutputHttpURL = s.buildOutputHttpURL(path)
		list = append(list, item)
	}
	return list
}
