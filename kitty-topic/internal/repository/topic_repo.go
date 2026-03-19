package repository

import (
	"strings"
	"time"

	"kitty-topic/internal/model"

	"github.com/google/uuid"
	"gorm.io/gorm"
)

type TopicRepo struct {
	db *gorm.DB
}

func NewTopicRepo(db *gorm.DB) *TopicRepo {
	return &TopicRepo{db: db}
}

func (r *TopicRepo) CreateTopic(t *model.Topic) error {
	if r.db == nil {
		return gorm.ErrInvalidDB
	}
	if t == nil {
		return gorm.ErrInvalidData
	}
	if strings.TrimSpace(t.ID) == "" {
		t.ID = uuid.NewString()
	}
	t.Tags = normalizeTags(t.Tags)
	return r.db.Create(t).Error
}

func (r *TopicRepo) GetTopic(id string) (*model.Topic, error) {
	if r.db == nil {
		return nil, gorm.ErrInvalidDB
	}
	var t model.Topic
	if err := r.db.First(&t, "id = ?", id).Error; err != nil {
		return nil, err
	}
	return &t, nil
}

func (r *TopicRepo) ListTopics(page, size int, searchKey string, status *int) ([]model.Topic, int64, error) {
	if r.db == nil {
		return nil, 0, gorm.ErrInvalidDB
	}
	if page <= 0 {
		page = 1
	}
	if size <= 0 {
		size = 20
	}

	query := r.db.Model(&model.Topic{})
	if strings.TrimSpace(searchKey) != "" {
		like := "%" + strings.TrimSpace(searchKey) + "%"
		query = query.Where("title LIKE ? OR source LIKE ?", like, like)
	}
	if status != nil {
		query = query.Where("status = ?", *status)
	}

	var total int64
	if err := query.Count(&total).Error; err != nil {
		return nil, 0, err
	}

	offset := (page - 1) * size
	var list []model.Topic
	if err := query.Order("created_at desc").Offset(offset).Limit(size).Find(&list).Error; err != nil {
		return nil, 0, err
	}
	return list, total, nil
}

func (r *TopicRepo) UpdateTopic(t *model.Topic) error {
	if r.db == nil {
		return gorm.ErrInvalidDB
	}
	if t == nil || strings.TrimSpace(t.ID) == "" {
		return gorm.ErrInvalidData
	}

	updates := map[string]interface{}{
		"title":      t.Title,
		"source":     t.Source,
		"content":    t.Content,
		"tags":       normalizeTags(t.Tags),
		"updated_by": t.UpdatedBy,
	}
	return r.db.Model(&model.Topic{}).Where("id = ?", t.ID).Updates(updates).Error
}

func (r *TopicRepo) DeleteTopic(id string) error {
	if r.db == nil {
		return gorm.ErrInvalidDB
	}
	return r.db.Where("id = ?", id).Delete(&model.Topic{}).Error
}

func (r *TopicRepo) UpdateTopicStatus(id string, newStatus int, publishedAt *time.Time, updatedBy string) error {
	if r.db == nil {
		return gorm.ErrInvalidDB
	}
	updates := map[string]interface{}{
		"status":     newStatus,
		"updated_by": updatedBy,
	}
	if publishedAt != nil {
		updates["published_at"] = publishedAt
	}
	return r.db.Model(&model.Topic{}).Where("id = ?", id).Updates(updates).Error
}

func normalizeTags(tags string) string {
	tags = strings.TrimSpace(tags)
	if tags == "" {
		return ""
	}
	parts := strings.Split(tags, ",")
	out := make([]string, 0, len(parts))
	for _, p := range parts {
		s := strings.TrimSpace(p)
		if s == "" {
			continue
		}
		out = append(out, s)
	}
	return strings.Join(out, ",")
}
