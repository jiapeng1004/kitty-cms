package engine

import "kitty-transcoder-go/internal/model"

// StepContext 步骤执行上下文
type StepContext struct {
	TaskID             string
	WorkDir            string
	ResolvedOutputPath string
	StepOutputs        map[int]string
	ReportProgress     func(stepIndex int, percent int) // 由引擎包装为整体 0-100
	Cancelled          func() bool
	TotalSteps         int // 可执行步骤数，用于计算整体进度
	CurrentStepIndex   int // 当前步骤下标 0..TotalSteps-1
}

// StepParam 步骤参数（从 strategy step 的 param JSON 解析），嵌入 model.StrategyStepParam 以支持 JSON
type StepParam struct {
	model.StrategyStepParam
}

func (p *StepParam) Resolution() string {
	if p.StrategyStepParam.Resolution != "" {
		return p.StrategyStepParam.Resolution
	}
	return "1920x1080"
}

func (p *StepParam) Bitrate() int {
	if p.StrategyStepParam.Bitrate > 0 {
		return p.StrategyStepParam.Bitrate
	}
	return 5000
}

func (p *StepParam) FrameRate() int {
	if p.StrategyStepParam.FrameRate > 0 {
		return p.StrategyStepParam.FrameRate
	}
	return 30
}

func (p *StepParam) Encoder() string {
	if p.StrategyStepParam.Encoder != "" {
		return p.StrategyStepParam.Encoder
	}
	return "libx264"
}

func (p *StepParam) TargetFormat() string {
	if p.StrategyStepParam.TargetFormat != "" {
		return p.StrategyStepParam.TargetFormat
	}
	return "mp4"
}

func (p *StepParam) FrameInterval() int {
	if p.StrategyStepParam.FrameInterval > 0 {
		return p.StrategyStepParam.FrameInterval
	}
	return 30
}

func (p *StepParam) ExtractFrameCount() int {
	if p.StrategyStepParam.ExtractFrameCount > 0 {
		return p.StrategyStepParam.ExtractFrameCount
	}
	return 1
}

func (p *StepParam) ExtractOutputFormat() string {
	if p.StrategyStepParam.ExtractOutputFormat != "" {
		return p.StrategyStepParam.ExtractOutputFormat
	}
	return "jpg"
}

func (p *StepParam) SpriteColumns() int {
	if p.StrategyStepParam.SpriteColumns > 0 {
		return p.StrategyStepParam.SpriteColumns
	}
	return 4
}

func (p *StepParam) SpriteRows() int {
	if p.StrategyStepParam.SpriteRows > 0 {
		return p.StrategyStepParam.SpriteRows
	}
	return 3
}

func (p *StepParam) SpriteScale() int {
	if p.StrategyStepParam.SpriteScale > 0 {
		return p.StrategyStepParam.SpriteScale
	}
	return 4
}

func (p *StepParam) ImageTargetFormat() string {
	if p.StrategyStepParam.ImageTargetFormat != "" {
		return p.StrategyStepParam.ImageTargetFormat
	}
	return "webp"
}

func (p *StepParam) ImageQuality() int {
	if p.StrategyStepParam.ImageQuality > 0 {
		return p.StrategyStepParam.ImageQuality
	}
	return 85
}
