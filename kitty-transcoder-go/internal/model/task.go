package model

import "time"

// TranscodeTask 转码任务表，与 Java 版 transcode_task 对齐
type TranscodeTask struct {
	ID                 string     `gorm:"column:id;primaryKey;size:64"`
	TaskType           string     `gorm:"column:task_type;size:32;default:SCHEDULED_TRANSCODE"`
	InputType          string     `gorm:"column:input_type;size:16;not null"`
	InputPath          string     `gorm:"column:input_path;size:1024;not null"`
	StrategyID         *string    `gorm:"column:strategy_id;size:64"`
	Status             string     `gorm:"column:status;size:32;default:PENDING"`
	Progress           int        `gorm:"column:progress;default:0"`
	ProgressDetail     string     `gorm:"column:progress_detail;type:text"`
	OutputPath         string     `gorm:"column:output_path;size:1024"`
	OutputHttpURL      string     `gorm:"column:output_http_url;size:1024"`
	StepOutputs        string     `gorm:"column:step_outputs;type:text"` // JSON: {"1":"path/1080p.mp4","2":"path/480p.mp4"}
	ErrorMessage       string     `gorm:"column:error_message;type:text"`
	WatermarkURL       string     `gorm:"column:watermark_url;size:1024"`
	WatermarkPosition  string     `gorm:"column:watermark_position;size:64"`
	Priority           int        `gorm:"column:priority;default:5"`
	RetryCount         int        `gorm:"column:retry_count;default:0"`
	NotificationConfig string     `gorm:"column:notification_config;type:text"`
	CreatedAt          time.Time  `gorm:"column:created_at;not null"`
	StartedAt          *time.Time `gorm:"column:started_at"`
	CompletedAt        *time.Time `gorm:"column:completed_at"`
	CreatedByAK        string     `gorm:"column:created_by_ak;size:64"`
}

func (TranscodeTask) TableName() string { return "transcode_task" }
