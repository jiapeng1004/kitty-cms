package engine

import (
	"context"
	"encoding/json"
	"fmt"
	"path/filepath"
	"sort"

	"gorm.io/gorm"
	"kitty-transcoder-go/internal/model"
)

// TranscodeResult 转码结果：主输出 + 各步骤输出
type TranscodeResult struct {
	MainOutput  string
	StepOutputs map[int]string
}

// Engine 转码引擎：按策略执行步骤
type Engine struct {
	db        *gorm.DB
	workDir   string
	executors map[string]StepExecutor
}

// NewEngine 创建引擎
func NewEngine(db *gorm.DB, workDir string) *Engine {
	e := &Engine{db: db, workDir: workDir, executors: make(map[string]StepExecutor)}
	e.executors["transcode"] = TranscodeStep{}
	e.executors["extract_frames"] = ExtractFramesStep{}
	e.executors["sprite"] = SpriteStep{}
	e.executors["image_convert"] = ImageConvertStep{}
	return e
}

// Transcode 执行策略转码，返回主输出路径与各步骤输出
func (e *Engine) Transcode(ctx context.Context, taskID, inputPath, strategyID string, reportProgress func(stepID int, percent int), cancelled func() bool) (*TranscodeResult, error) {
	steps, err := e.loadStrategySteps(strategyID)
	if err != nil || len(steps) == 0 {
		return nil, err
	}
	workDir := e.workDir
	if workDir == "" {
		workDir = filepath.Dir(inputPath)
	}
	execSteps := make([]*model.TranscodeStrategyStep, 0, len(steps))
	for _, step := range steps {
		if _, ok := e.executors[step.Type]; ok {
			execSteps = append(execSteps, step)
		}
	}
	totalSteps := len(execSteps)
	if totalSteps == 0 {
		totalSteps = 1
	}
	stepOutputs := make(map[int]string)
	runCtx := &StepContext{
		TaskID:           taskID,
		WorkDir:          workDir,
		StepOutputs:      stepOutputs,
		Cancelled:        cancelled,
		TotalSteps:       totalSteps,
		CurrentStepIndex: 0,
	}
	if reportProgress != nil {
		runCtx.ReportProgress = func(stepIndex int, percent int) {
			overall := (runCtx.CurrentStepIndex*100 + percent) / runCtx.TotalSteps
			if overall > 100 {
				overall = 100
			}
			reportProgress(stepIndex, overall)
		}
	}
	currentInput := inputPath
	mainStepID := e.getMainOutputStepID(steps)
	stepIndex := 0
	for _, step := range steps {
		if cancelled != nil && cancelled() {
			return nil, context.Canceled
		}
		exec, ok := e.executors[step.Type]
		if !ok {
			continue
		}
		runCtx.CurrentStepIndex = stepIndex
		stepIndex++
		stepSuffix := fmt.Sprintf("_step%d", step.StepID)
		runCtx.ResolvedOutputPath = ""
		outPath, err := exec.Execute(ctx, currentInput, step, stepSuffix, runCtx)
		if err != nil {
			return nil, err
		}
		stepOutputs[step.StepID] = outPath
		if reportProgress != nil {
			reportProgress(step.StepID, 100)
		}
		currentInput = outPath
	}
	mainOutput := ""
	if mainStepID > 0 && stepOutputs[mainStepID] != "" {
		mainOutput = stepOutputs[mainStepID]
	} else if len(stepOutputs) > 0 {
		ids := make([]int, 0, len(stepOutputs))
		for id := range stepOutputs {
			ids = append(ids, id)
		}
		sort.Ints(ids)
		mainOutput = stepOutputs[ids[len(ids)-1]]
	}
	return &TranscodeResult{MainOutput: mainOutput, StepOutputs: stepOutputs}, nil
}

func (e *Engine) loadStrategySteps(strategyID string) ([]*model.TranscodeStrategyStep, error) {
	var first model.TranscodeStrategyStep
	err := e.db.Where("strategy_name = ?", strategyID).First(&first).Error
	if err != nil {
		return nil, err
	}
	rootID := first.RootID
	var steps []*model.TranscodeStrategyStep
	if err := e.db.Where("root_id = ?", rootID).Order("step_id").Find(&steps).Error; err != nil {
		return nil, err
	}
	return steps, nil
}

func (e *Engine) getMainOutputStepID(steps []*model.TranscodeStrategyStep) int {
	for _, s := range steps {
		if s.Type == "transcode" {
			return s.StepID
		}
	}
	if len(steps) > 0 {
		return steps[len(steps)-1].StepID
	}
	return 0
}

// StepOutputsJSON 将 map[int]string 序列化为 JSON 字符串
func StepOutputsJSON(m map[int]string) string {
	if len(m) == 0 {
		return ""
	}
	b, _ := json.Marshal(m)
	return string(b)
}
