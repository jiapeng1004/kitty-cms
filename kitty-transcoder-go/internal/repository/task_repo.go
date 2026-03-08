package repository

import (
	"gorm.io/gorm"
	"kitty-transcoder-go/internal/model"
)

type TaskRepo struct {
	db *gorm.DB
}

func NewTaskRepo(db *gorm.DB) *TaskRepo {
	return &TaskRepo{db: db}
}

func (r *TaskRepo) Create(t *model.TranscodeTask) error {
	return r.db.Create(t).Error
}

func (r *TaskRepo) GetByID(id string) (*model.TranscodeTask, error) {
	var t model.TranscodeTask
	err := r.db.Where("id = ?", id).First(&t).Error
	if err != nil {
		return nil, err
	}
	return &t, nil
}

func (r *TaskRepo) UpdateStatus(id, status string, progress int, stepOutputs string) error {
	up := map[string]interface{}{"status": status, "progress": progress}
	if stepOutputs != "" {
		up["step_outputs"] = stepOutputs
	}
	return r.db.Model(&model.TranscodeTask{}).Where("id = ?", id).Updates(up).Error
}

func (r *TaskRepo) UpdateOutput(id, outputPath, outputHttpURL, stepOutputs string) error {
	up := map[string]interface{}{
		"status": "COMPLETED", "progress": 100,
		"output_path": outputPath, "output_http_url": outputHttpURL,
		"step_outputs": stepOutputs,
	}
	return r.db.Model(&model.TranscodeTask{}).Where("id = ?", id).Updates(up).Error
}

func (r *TaskRepo) UpdateError(id, errMsg string) error {
	return r.db.Model(&model.TranscodeTask{}).Where("id = ?", id).Updates(map[string]interface{}{
		"status": "FAILED", "error_message": errMsg,
	}).Error
}

func (r *TaskRepo) List(page, pageSize int, status, taskType string) ([]model.TranscodeTask, int64, error) {
	var total int64
	q := r.db.Model(&model.TranscodeTask{})
	if status != "" {
		q = q.Where("status = ?", status)
	}
	if taskType != "" {
		q = q.Where("task_type = ?", taskType)
	}
	if err := q.Count(&total).Error; err != nil {
		return nil, 0, err
	}
	var list []model.TranscodeTask
	offset := (page - 1) * pageSize
	if offset < 0 {
		offset = 0
	}
	if pageSize <= 0 {
		pageSize = 20
	}
	err := q.Order("created_at DESC").Offset(offset).Limit(pageSize).Find(&list).Error
	return list, total, err
}

func (r *TaskRepo) Cancel(id string) error {
	return r.db.Model(&model.TranscodeTask{}).Where("id = ?", id).Update("status", "CANCELLED").Error
}
