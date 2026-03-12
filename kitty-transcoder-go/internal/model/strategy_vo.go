package model

// StrategyVO 与 Java StrategyVO 对齐，API 返回用；id 为 root_id（long）
type StrategyVO struct {
	ID           int64            `json:"id"`
	Name         string           `json:"name"`
	StepCount    int              `json:"stepCount,omitempty"`
	Steps        []StrategyStepVO `json:"steps,omitempty"`
	WorkDir      string           `json:"workDir,omitempty"`
	CreatedAt    int64            `json:"createdAt,omitempty"`
	TargetFormat string           `json:"targetFormat,omitempty"`
	Resolution   string           `json:"resolution,omitempty"`
	Bitrate      int              `json:"bitrate,omitempty"`
	FrameRate    int              `json:"frameRate,omitempty"`
	Encoder      string           `json:"encoder,omitempty"`
}

// StrategyStepVO 与 Java StrategyStepVO 对齐
type StrategyStepVO struct {
	StepID              int    `json:"stepId"`
	Type                string `json:"type"`
	Depends             string `json:"depends,omitempty"`
	InputTemplate       string `json:"inputTemplate,omitempty"`
	OutputTemplate      string `json:"outputTemplate,omitempty"`
	TargetFormat        string `json:"targetFormat,omitempty"`
	Resolution          string `json:"resolution,omitempty"`
	Bitrate             int    `json:"bitrate,omitempty"`
	FrameRate           int    `json:"frameRate,omitempty"`
	Encoder             string `json:"encoder,omitempty"`
	FrameInterval       int    `json:"frameInterval,omitempty"`
	ExtractFrameCount   int    `json:"extractFrameCount,omitempty"`
	ExtractOutputFormat string `json:"extractOutputFormat,omitempty"`
	SpriteColumns       int    `json:"spriteColumns,omitempty"`
	SpriteRows          int    `json:"spriteRows,omitempty"`
	SpriteScale         int    `json:"spriteScale,omitempty"`
	ImageTargetFormat   string `json:"imageTargetFormat,omitempty"`
	ImageQuality        int    `json:"imageQuality,omitempty"`
	ImageResize         string `json:"imageResize,omitempty"`
	Condition           string `json:"condition,omitempty"`
	StrategyIDWhenTrue  int64  `json:"strategyIdWhenTrue,omitempty"`
	StrategyIDWhenFalse int64  `json:"strategyIdWhenFalse,omitempty"`
}
