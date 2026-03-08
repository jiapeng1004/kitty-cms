package model

// StrategyStepParam 策略步骤参数（与 Java StrategyStepVO 字段对应，存 JSON）
type StrategyStepParam struct {
	StepID              int    `json:"stepId"`
	Type                string `json:"type"`
	Depends             string `json:"depends"`
	InputTemplate       string `json:"inputTemplate"`
	OutputTemplate      string `json:"outputTemplate"`
	TargetFormat        string `json:"targetFormat"`
	Resolution          string `json:"resolution"`
	Bitrate             int    `json:"bitrate"`
	FrameRate           int    `json:"frameRate"`
	Encoder             string `json:"encoder"`
	FrameInterval       int    `json:"frameInterval"`
	ExtractFrameCount   int    `json:"extractFrameCount"`
	ExtractOutputFormat string `json:"extractOutputFormat"`
	SpriteColumns       int    `json:"spriteColumns"`
	SpriteRows          int    `json:"spriteRows"`
	SpriteScale         int    `json:"spriteScale"`
	ImageTargetFormat   string `json:"imageTargetFormat"`
	ImageQuality        int    `json:"imageQuality"`
	ImageResize         string `json:"imageResize"`
}

// StepOutputItem 单步骤输出，用于回调
type StepOutputItem struct {
	StepID        int    `json:"stepId"`
	StepType      string `json:"stepType"`
	OutputPath    string `json:"outputPath"`
	OutputHttpURL string `json:"outputHttpUrl,omitempty"`
}

// CreateTaskReq 创建任务请求
type CreateTaskReq struct {
	InputType          string `json:"inputType" v:"required"`
	InputPath          string `json:"inputPath" v:"required"`
	StrategyID         string `json:"strategyId"`
	WatermarkURL       string `json:"watermarkUrl"`
	WatermarkPosition  string `json:"watermarkPosition"`
	Priority           int    `json:"priority"`
	NotificationConfig string `json:"notificationConfig"`
}

// ListTasksReq 分页列表请求
type ListTasksReq struct {
	Page     int    `json:"page" d:"1"`
	PageSize int    `json:"pageSize" d:"20"`
	Status   string `json:"status"`
	TaskType string `json:"taskType"`
}
