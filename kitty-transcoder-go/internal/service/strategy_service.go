package service

import (
	"encoding/json"
	"time"

	"gorm.io/gorm"
	"kitty-transcoder-go/internal/model"
	"kitty-transcoder-go/internal/repository"
)

type StrategyService struct {
	repo *repository.StrategyRepo
	db   *gorm.DB
}

func NewStrategyService(db *gorm.DB, repo *repository.StrategyRepo) *StrategyService {
	return &StrategyService{repo: repo, db: db}
}

func (s *StrategyService) CreateStrategy(name string, steps []model.StrategyStepParam) (rootID int64, err error) {
	if len(steps) == 0 {
		return 0, gorm.ErrRecordNotFound
	}
	now := time.Now()
	rootID = now.UnixNano()
	if rootID <= 0 {
		rootID = -rootID
	}
	for _, sp := range steps {
		paramJSON, _ := json.Marshal(sp)
		step := &model.TranscodeStrategyStep{
			RootID:       rootID,
			StrategyName: name,
			StepID:       sp.StepID,
			Depends:      sp.Depends,
			Type:         sp.Type,
			Param:        string(paramJSON),
			CreatedAt:    now,
		}
		if step.Type == "" {
			step.Type = "transcode"
		}
		if err := s.repo.CreateStep(step); err != nil {
			return 0, err
		}
	}
	return rootID, nil
}

func (s *StrategyService) GetStrategy(strategyID string) ([]model.TranscodeStrategyStep, error) {
	first, err := s.repo.GetFirstByStrategyName(strategyID)
	if err != nil {
		return nil, err
	}
	return s.repo.GetByRootID(first.RootID)
}

func (s *StrategyService) ListStrategies() ([]int64, error) {
	return s.repo.ListRootIDs()
}

func (s *StrategyService) ListStrategyNames() ([]string, error) {
	return s.repo.ListStrategyNames()
}

func (s *StrategyService) DeleteStrategy(strategyID string) error {
	first, err := s.repo.GetFirstByStrategyName(strategyID)
	if err != nil {
		return err
	}
	return s.repo.DeleteByRootID(first.RootID)
}
