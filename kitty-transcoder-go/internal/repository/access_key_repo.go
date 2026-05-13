package repository

import (
	"gorm.io/gorm"
	"kitty-transcoder-go/internal/model"
)

type AccessKeyRepo struct {
	db *gorm.DB
}

func NewAccessKeyRepo(db *gorm.DB) *AccessKeyRepo {
	return &AccessKeyRepo{db: db}
}

func (r *AccessKeyRepo) Create(ak *model.TranscodeAccessKey) error {
	return r.db.Create(ak).Error
}

func (r *AccessKeyRepo) GetByAccessKeyID(accessKeyID string) (*model.TranscodeAccessKey, error) {
	var ak model.TranscodeAccessKey
	err := r.db.Where("access_key_id = ?", accessKeyID).First(&ak).Error
	if err != nil {
		return nil, err
	}
	return &ak, nil
}

func (r *AccessKeyRepo) List() ([]model.TranscodeAccessKey, error) {
	var list []model.TranscodeAccessKey
	err := r.db.Order("created_at DESC").Find(&list).Error
	return list, err
}

func (r *AccessKeyRepo) DeleteByAccessKeyID(accessKeyID string) error {
	return r.db.Where("access_key_id = ?", accessKeyID).Delete(&model.TranscodeAccessKey{}).Error
}
