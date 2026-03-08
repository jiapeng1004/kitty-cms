package model

import "time"

// TranscodeStrategyStep 策略步骤表，与 Java 版 transcode_strategy_step 对齐
// 策略由 root_id 区分，同一 root_id 的步骤组成一个策略
type TranscodeStrategyStep struct {
	ID           int64      `gorm:"column:id;primaryKey;autoIncrement"`
	RootID       int64      `gorm:"column:root_id;not null;uniqueIndex:uk_strategy_step,priority:1"`
	StrategyName string     `gorm:"column:strategy_name;size:200"`
	WorkDir      string     `gorm:"column:work_dir;size:1024"`
	StepID       int        `gorm:"column:step_id;not null;uniqueIndex:uk_strategy_step,priority:2"`
	Depends      string     `gorm:"column:depends;size:256"`
	Type         string     `gorm:"column:type;size:64;not null"` // transcode, extract_frames, sprite, image_convert
	TiAnchor     string     `gorm:"column:ti_anchor;size:64"`
	Param        string     `gorm:"column:param;type:text"` // JSON 步骤参数
	CreatedAt    time.Time  `gorm:"column:created_at;not null"`
	UpdatedAt    *time.Time `gorm:"column:updated_at"`
}

func (TranscodeStrategyStep) TableName() string { return "transcode_strategy_step" }
