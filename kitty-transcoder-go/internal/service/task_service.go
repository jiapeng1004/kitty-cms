package service

import (
	"context"
	"encoding/json"
	"errors"
	"fmt"
	"os"
	"path/filepath"
	"sort"
	"strconv"
	"strings"
	"time"

	"github.com/google/uuid"
	"gorm.io/gorm"
	"kitty-transcoder-go/internal/broadcast"
	"kitty-transcoder-go/internal/cancel"
	"kitty-transcoder-go/internal/config"
	"kitty-transcoder-go/internal/engine"
	"kitty-transcoder-go/internal/model"
	"kitty-transcoder-go/internal/repository"
)

type TaskService struct {
	taskRepo            *repository.TaskRepo
	strategyRepo        *repository.StrategyRepo
	engine              *engine.Engine
	cfg                 *config.Transcoder
	db                  *gorm.DB
	cancelRegistry      *cancel.Registry
	progressBroadcaster *broadcast.ProgressBroadcaster
}

func NewTaskService(db *gorm.DB, taskRepo *repository.TaskRepo, strategyRepo *repository.StrategyRepo, eng *engine.Engine, cfg *config.Transcoder, cancelRegistry *cancel.Registry, progressBroadcaster *broadcast.ProgressBroadcaster) *TaskService {
	if cancelRegistry == nil {
		cancelRegistry = cancel.NewRegistry(nil)
	}
	return &TaskService{taskRepo: taskRepo, strategyRepo: strategyRepo, engine: eng, cfg: cfg, db: db, cancelRegistry: cancelRegistry, progressBroadcaster: progressBroadcaster}
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
	if req.StrategyID != "" {
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

func (s *TaskService) ListTasks(page, pageSize int, status, taskType string) ([]model.TranscodeTask, int64, error) {
	return s.taskRepo.List(page, pageSize, status, taskType)
}

func (s *TaskService) CancelTask(id string) error {
	s.cancelRegistry.MarkCancelled(id)
	return s.taskRepo.Cancel(id)
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
	s.db.Model(&model.TranscodeTask{}).Where("id = ?", taskID).Updates(map[string]interface{}{
		"status": "PROCESSING", "started_at": now, "progress": 0,
	})
	inputPath := task.InputPath
	if task.StrategyID == nil || *task.StrategyID == "" {
		return fmt.Errorf("task has no strategy_id")
	}
	workDir := s.cfg.WorkDir
	if workDir == "" {
		workDir = os.TempDir()
	}
	cancelled := func() bool { return s.cancelRegistry.IsCancelled(taskID) }
	reportProgress := func(stepID int, percent int) {
		s.taskRepo.UpdateStatus(taskID, "PROCESSING", percent, "")
		if s.progressBroadcaster != nil {
			if vo, _ := s.GetProgressVO(taskID); vo != nil {
				s.progressBroadcaster.Broadcast(*vo)
			}
		}
	}
	result, err := s.engine.Transcode(ctx, taskID, inputPath, *task.StrategyID, reportProgress, cancelled)
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
	err = s.db.Model(&model.TranscodeTask{}).Where("id = ?", taskID).Updates(map[string]interface{}{
		"status": "COMPLETED", "progress": 100,
		"output_path": outputPath, "output_http_url": outputHttpURL,
		"step_outputs": stepOutputsJSON, "completed_at": completedAt, "error_message": "",
	}).Error
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
	prefix := strings.TrimSuffix(s.cfg.Output.HttpPrefix, "/")
	normalized := filepath.ToSlash(outputPath)
	if s.cfg.WorkDir != "" {
		workSlash := filepath.ToSlash(s.cfg.WorkDir)
		if strings.HasPrefix(normalized, workSlash+"/") {
			normalized = strings.TrimPrefix(normalized, workSlash+"/")
		} else if normalized == workSlash {
			normalized = ""
		}
	}
	if normalized == "" {
		return prefix + "/"
	}
	return prefix + "/" + normalized
}

// BuildStepOutputsList 用于回调：根据 step_outputs JSON 构建 StepOutputItem 列表
func (s *TaskService) BuildStepOutputsList(stepOutputsJSON string, strategyID string) []model.StepOutputItem {
	if stepOutputsJSON == "" {
		return nil
	}
	var m map[string]string
	if err := json.Unmarshal([]byte(stepOutputsJSON), &m); err != nil {
		return nil
	}
	stepTypeByID := make(map[int]string)
	if strategyID != "" {
		first, _ := s.strategyRepo.GetFirstByStrategyName(strategyID)
		if first != nil {
			steps, _ := s.strategyRepo.GetByRootID(first.RootID)
			for _, st := range steps {
				stepTypeByID[st.StepID] = st.Type
			}
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
