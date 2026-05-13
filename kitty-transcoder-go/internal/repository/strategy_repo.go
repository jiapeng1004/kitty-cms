package repository

import (
	"kitty-transcoder-go/internal/model"

	"gorm.io/gorm"
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

// GetFirstByRootID 按 root_id 取任意一条步骤（兼容任务里存的是 root_id 的历史数据）
func (r *StrategyRepo) GetFirstByRootID(rootID int64) (*model.TranscodeStrategyStep, error) {
	var s model.TranscodeStrategyStep
	err := r.db.Where("root_id = ?", rootID).Order("step_id").First(&s).Error
	if err != nil {
		return nil, err
	}
	return &s, nil
}

// GetStepsByStrategyID 按策略 ID（root_id）加载步骤，所有 DB 访问集中在此。
func (r *StrategyRepo) GetStepsByStrategyID(strategyID int64) ([]*model.TranscodeStrategyStep, error) {
	if strategyID <= 0 {
		return nil, gorm.ErrRecordNotFound
	}
	list, err := r.GetByRootID(strategyID)
	if err != nil {
		return nil, err
	}
	return sliceToPtrs(list), nil
}

func sliceToPtrs(list []model.TranscodeStrategyStep) []*model.TranscodeStrategyStep {
	if len(list) == 0 {
		return nil
	}
	out := make([]*model.TranscodeStrategyStep, len(list))
	for i := range list {
		out[i] = &list[i]
	}
	return out
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
