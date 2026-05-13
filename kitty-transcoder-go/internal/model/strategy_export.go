package model

type StrategyExportFormat struct {
	FormatVersion int                  `yaml:"format_version"`
	StrategyID    *string              `yaml:"strategy_id,omitempty"`
	Name          string               `yaml:"name"`
	WorkDir       string               `yaml:"workDir,omitempty"`
	Steps         []StrategyStepExport `yaml:"steps,omitempty"`
}

type StrategyStepExport struct {
	StepID              *int   `yaml:"stepId,omitempty"`
	Type                string `yaml:"type"`
	Depends             string `yaml:"depends,omitempty"`
	InputTemplate       string `yaml:"inputTemplate,omitempty"`
	OutputTemplate      string `yaml:"outputTemplate,omitempty"`
	TargetFormat        string `yaml:"targetFormat,omitempty"`
	Resolution          string `yaml:"resolution,omitempty"`
	Bitrate             *int   `yaml:"bitrate,omitempty"`
	FrameRate           *int   `yaml:"frameRate,omitempty"`
	Encoder             string `yaml:"encoder,omitempty"`
	FrameInterval       *int   `yaml:"frameInterval,omitempty"`
	ExtractFrameCount   *int   `yaml:"extractFrameCount,omitempty"`
	ExtractOutputFormat string `yaml:"extractOutputFormat,omitempty"`
	SpriteColumns       *int   `yaml:"spriteColumns,omitempty"`
	SpriteRows          *int   `yaml:"spriteRows,omitempty"`
	SpriteScale         *int   `yaml:"spriteScale,omitempty"`
	ImageTargetFormat   string `yaml:"imageTargetFormat,omitempty"`
	ImageQuality        *int   `yaml:"imageQuality,omitempty"`
	ImageResize         string `yaml:"imageResize,omitempty"`
	Condition           string `yaml:"condition,omitempty"`
	StrategyIDWhenTrue  *int64 `yaml:"strategyIdWhenTrue,omitempty"`
	StrategyIDWhenFalse *int64 `yaml:"strategyIdWhenFalse,omitempty"`
}
