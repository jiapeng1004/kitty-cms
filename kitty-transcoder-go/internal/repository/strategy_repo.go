package repository

import (
	"gorm.io/gorm"
	"kitty-transcoder-go/internal/model"
)

type StrategyRepo struct {
	db *gorm.DB
}

func NewStrategyRepo(db *gorm.DB) *StrategyRepo {
	return &StrategyRepo{db: db}
}

func (r *StrategyRepo) CreateStep(s *model.TranscodeStrategyStep) error {
	return r.db.Create(s).Error
}

func (r *StrategyRepo) GetByRootID(rootID int64) ([]model.TranscodeStrategyStep, error) {
	var list []model.TranscodeStrategyStep
	err := r.db.Where("root_id = ?", rootID).Order("step_id").Find(&list).Error
	return list, err
}

func (r *StrategyRepo) GetFirstByStrategyName(name string) (*model.TranscodeStrategyStep, error) {
	var s model.TranscodeStrategyStep
	err := r.db.Where("strategy_name = ?", name).First(&s).Error
	if err != nil {
		return nil, err
	}
	return &s, nil
}

func (r *StrategyRepo) ListRootIDs() ([]int64, error) {
	var ids []int64
	err := r.db.Model(&model.TranscodeStrategyStep{}).Distinct("root_id").Pluck("root_id", &ids).Error
	return ids, err
}

func (r *StrategyRepo) ListStrategyNames() ([]string, error) {
	var names []string
	err := r.db.Model(&model.TranscodeStrategyStep{}).Distinct("strategy_name").Where("strategy_name != ?", "").Pluck("strategy_name", &names).Error
	return names, err
}

func (r *StrategyRepo) DeleteByRootID(rootID int64) error {
	return r.db.Where("root_id = ?", rootID).Delete(&model.TranscodeStrategyStep{}).Error
}

func (r *StrategyRepo) UpdateStep(s *model.TranscodeStrategyStep) error {
	return r.db.Save(s).Error
}
