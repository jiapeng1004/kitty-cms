package engine

import (
	"context"
	"encoding/json"
	"fmt"
	"path/filepath"
	"sort"
	"strconv"
	"strings"

	"kitty-transcoder-go/internal/model"
)

// StrategyLoader 策略步骤加载器，由 repository 实现，engine 不直接访问 DB
type StrategyLoader interface {
	GetStepsByStrategyID(strategyID int64) ([]*model.TranscodeStrategyStep, error)
}

// TranscodeResult 转码结果：主输出 + 各步骤输出
type TranscodeResult struct {
	MainOutput  string
	StepOutputs map[int]string
}

// Engine 转码引擎：按策略执行步骤，仅依赖 StrategyLoader 不写 SQL
type Engine struct {
	strategyLoader StrategyLoader
	workDir        string
	executors      map[string]StepExecutor
}

// NewEngine 创建引擎
func NewEngine(strategyLoader StrategyLoader, workDir string) *Engine {
	e := &Engine{strategyLoader: strategyLoader, workDir: workDir, executors: make(map[string]StepExecutor)}
	e.executors["transcode"] = TranscodeStep{}
	e.executors["extract_frames"] = ExtractFramesStep{}
	e.executors["sprite"] = SpriteStep{}
	e.executors["image_convert"] = ImageConvertStep{}
	return e
}

// Transcode 执行策略转码，返回主输出路径与各步骤输出。
// runWorkDir 为本任务使用的工作目录（策略或全局），非空时优先于 e.workDir，与 Java ctx.setWorkDir 一致。
func (e *Engine) Transcode(ctx context.Context, taskID, inputPath string, strategyID int64, runWorkDir string, reportProgress func(stepID int, percent int), cancelled func() bool) (*TranscodeResult, error) {
	steps, err := e.loadStrategySteps(strategyID)
	if err != nil || len(steps) == 0 {
		return nil, err
	}
	workDir := runWorkDir
	if workDir == "" {
		workDir = e.workDir
	}
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
	mainStepID := findLastStepByTopology(execSteps)
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

func (e *Engine) loadStrategySteps(strategyID int64) ([]*model.TranscodeStrategyStep, error) {
	return e.strategyLoader.GetStepsByStrategyID(strategyID)
}

// findLastStepByTopology 与 Java 一致：在无依赖的叶子步骤中选主输出步骤；
// 多输出时优先选 transcode，其次其他类型，避免雪碧图等被误当主输出。
func findLastStepByTopology(steps []*model.TranscodeStrategyStep) int {
	if len(steps) == 0 {
		return 0
	}
	stepByID := make(map[int]*model.TranscodeStrategyStep)
	stepIds := make([]int, 0, len(steps))
	for _, s := range steps {
		stepByID[s.StepID] = s
		stepIds = append(stepIds, s.StepID)
	}
	// 解析依赖：depsMap[sid] = 该步骤依赖的 stepId 列表
	depsMap := make(map[int][]int)
	for _, s := range steps {
		depsMap[s.StepID] = parseDepends(s.Depends)
	}
	// 被依赖的步骤 = 作为别人 dep 的 stepId
	hasDependent := make(map[int]bool)
	for _, deps := range depsMap {
		for _, d := range deps {
			hasDependent[d] = true
		}
	}
	// 叶子 = 没有任何步骤依赖它（即拓扑上的“最后”步骤）
	var leaves []int
	for _, sid := range stepIds {
		if !hasDependent[sid] {
			leaves = append(leaves, sid)
		}
	}
	if len(leaves) == 0 {
		return stepIds[len(stepIds)-1]
	}
	// 若所有步骤都是叶子（depends 未配置），按线性管道处理：主输出=最后一步
	if len(leaves) == len(stepIds) {
		return stepIds[len(stepIds)-1]
	}
	// 叶子中优先选 transcode（取 stepId 最小的，通常为主规格）
	transcodeFirst := -1
	for _, sid := range leaves {
		s := stepByID[sid]
		if s != nil && s.Type == "transcode" {
			if transcodeFirst < 0 || sid < transcodeFirst {
				transcodeFirst = sid
			}
		}
	}
	if transcodeFirst >= 0 {
		return transcodeFirst
	}
	// 否则取叶子中 stepId 最大的
	maxLeaf := leaves[0]
	for _, sid := range leaves[1:] {
		if sid > maxLeaf {
			maxLeaf = sid
		}
	}
	return maxLeaf
}

func parseDepends(depends string) []int {
	if depends == "" {
		return nil
	}
	parts := strings.Split(strings.TrimSpace(depends), ",")
	var out []int
	for _, p := range parts {
		p = strings.TrimSpace(p)
		if p == "" {
			continue
		}
		id, err := strconv.Atoi(p)
		if err != nil {
			continue
		}
		out = append(out, id)
	}
	return out
}

// StepOutputsJSON 将 map[int]string 序列化为 JSON 字符串
func StepOutputsJSON(m map[int]string) string {
	if len(m) == 0 {
		return ""
	}
	b, _ := json.Marshal(m)
	return string(b)
}
