package model

// TaskVO 与 Java TaskVO 对齐，API 返回用
type TaskVO struct {
	ID                string               `json:"id"`
	TaskType          string               `json:"taskType"`
	Status            string               `json:"status"`
	Progress          int                  `json:"progress"`
	InputType         string               `json:"inputType"`
	InputPath         string               `json:"inputPath"`
	InputFile         string               `json:"inputFile"`
	OutputPath        string               `json:"outputPath"`
	OutputFile        string               `json:"outputFile"`
	OutputHttpURL     string               `json:"outputHttpUrl"`
	StrategyID        int64                `json:"strategyId"`
	WatermarkURL      string               `json:"watermarkUrl"`
	WatermarkPosition string               `json:"watermarkPosition"`
	Notifications     []NotificationConfig `json:"notifications,omitempty"`
	CreatedAt         int64                `json:"createdAt"`
	StartedAt         int64                `json:"startedAt,omitempty"`
	CompletedAt       int64                `json:"completedAt,omitempty"`
	ErrorMessage      string               `json:"errorMessage,omitempty"`
}

// NotificationConfig 与 Java NotificationConfig 对齐（API 用 method/target，兼容 DB 中的 type）
type NotificationConfig struct {
	Method string `json:"method"`
	Target string `json:"target"`
	Type   string `json:"type,omitempty"` // 兼容旧数据：解析后填到 Method
}

// ListTasksResponse 分页任务列表响应，与 Java 对齐（含分页信息）
type ListTasksResponse struct {
	List     []TaskVO `json:"list"`
	Total    int64    `json:"total"`
	Page     int      `json:"page"`
	PageSize int      `json:"pageSize"`
}
