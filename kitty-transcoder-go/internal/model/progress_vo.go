package model

// ProgressVO 与 Java ProgressVO 对齐，用于 SSE 与回调
type ProgressVO struct {
	TaskID           string             `json:"taskId"`
	Progress         int                `json:"progress"`
	Status           string             `json:"status"`
	ProcessingTime   int                `json:"processingTime,omitempty"`
	CurrentStep      string             `json:"currentStep,omitempty"`
	TotalSteps       int                `json:"totalSteps,omitempty"`
	StepProgressList []StepProgressItem `json:"stepProgressList,omitempty"`
}

// StepProgressItem 与 Java StepProgressItem 对齐
type StepProgressItem struct {
	StepID   int    `json:"stepId"`
	Type     string `json:"type,omitempty"`
	Name     string `json:"name,omitempty"`
	Depends  string `json:"depends,omitempty"`
	Status   string `json:"status,omitempty"`
	Progress int    `json:"progress,omitempty"`
}
