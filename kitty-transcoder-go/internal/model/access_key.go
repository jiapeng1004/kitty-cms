package model

import "time"

// TranscodeAccessKey AK/SK 表，与 Java transcode_access_key 对齐
type TranscodeAccessKey struct {
	ID          int64      `gorm:"column:id;primaryKey;autoIncrement"`
	AccessKeyID string     `gorm:"column:access_key_id;size:64;not null;uniqueIndex"`
	SecretKey   string     `gorm:"column:secret_key;size:128;not null"`
	Name        string     `gorm:"column:name;size:100"`
	Status      string     `gorm:"column:status;size:20;default:ACTIVE"`
	ExpiresAt   *time.Time `gorm:"column:expires_at"`
	LastUsedAt  *time.Time `gorm:"column:last_used_at"`
	CreatedAt   time.Time  `gorm:"column:created_at;not null"`
	UpdatedAt   *time.Time `gorm:"column:updated_at"`
	Description string     `gorm:"column:description;size:500"`
}

func (TranscodeAccessKey) TableName() string { return "transcode_access_key" }
