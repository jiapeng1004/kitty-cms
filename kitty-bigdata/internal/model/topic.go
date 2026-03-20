package model

import (
	"time"
)

type TopicStatus int

const (
	TopicStatusDraft         TopicStatus = 0
	TopicStatusPendingReview TopicStatus = 1
	TopicStatusApproved      TopicStatus = 2
	TopicStatusPublished     TopicStatus = 3
)

func (s TopicStatus) String() string {
	switch s {
	case TopicStatusDraft:
		return "DRAFT"
	case TopicStatusPendingReview:
		return "PENDING_REVIEW"
	case TopicStatusApproved:
		return "APPROVED"
	case TopicStatusPublished:
		return "PUBLISHED"
	default:
		return "UNKNOWN"
	}
}

type Topic struct {
	ID          string     `gorm:"primaryKey;column:id;size:64"`
	Title       string     `gorm:"column:title;type:varchar(255);not null"`
	Source      string     `gorm:"column:source;type:varchar(255);not null"`
	Content     string     `gorm:"column:content;type:longtext"`
	Tags        string     `gorm:"column:tags;type:text"`
	Status      int        `gorm:"column:status;not null"`
	PublishedAt *time.Time `gorm:"column:published_at"`
	CreatedBy   string     `gorm:"column:created_by;type:varchar(128)"`
	UpdatedBy   string     `gorm:"column:updated_by;type:varchar(128)"`
	CreatedAt   time.Time  `gorm:"column:created_at;autoCreateTime"`
	UpdatedAt   time.Time  `gorm:"column:updated_at;autoUpdateTime"`
}

func (Topic) TableName() string {
	return "kt_topic"
}
