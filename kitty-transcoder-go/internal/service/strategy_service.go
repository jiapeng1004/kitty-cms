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

// GetStrategyVO 返回 StrategyVO（含 steps），与 Java getStrategy 对齐
func (s *StrategyService) GetStrategyVO(strategyID string) (*model.StrategyVO, error) {
	steps, err := s.GetStrategy(strategyID)
	if err != nil {
		return nil, err
	}
	vo := &model.StrategyVO{
		ID:        strategyID,
		Name:      strategyID,
		StepCount: len(steps),
		Steps:     make([]model.StrategyStepVO, 0, len(steps)),
	}
	for i := range steps {
		vo.Steps = append(vo.Steps, stepToStrategyStepVO(&steps[i]))
	}
	return vo, nil
}

// ListStrategiesVO 返回策略列表（id, name, stepCount），与 Java getStrategies 对齐。切勿返回 rootID 数组。
func (s *StrategyService) ListStrategiesVO() ([]model.StrategyVO, error) {
	names, err := s.repo.ListStrategyNames()
	if err != nil {
		return nil, err
	}
	out := make([]model.StrategyVO, 0, len(names))
	for _, name := range names {
		first, _ := s.repo.GetFirstByStrategyName(name)
		stepCount := 0
		if first != nil {
			steps, _ := s.repo.GetByRootID(first.RootID)
			stepCount = len(steps)
		}
		out = append(out, model.StrategyVO{
			ID:        name,
			Name:      name,
			StepCount: stepCount,
		})
	}
	return out, nil
}

func stepToStrategyStepVO(st *model.TranscodeStrategyStep) model.StrategyStepVO {
	vo := model.StrategyStepVO{
		StepID:  st.StepID,
		Type:    st.Type,
		Depends: st.Depends,
	}
	if st.Param != "" {
		var p model.StrategyStepParam
		_ = json.Unmarshal([]byte(st.Param), &p)
		vo.TargetFormat = p.TargetFormat
		vo.Resolution = p.Resolution
		vo.Bitrate = p.Bitrate
		vo.FrameRate = p.FrameRate
		vo.Encoder = p.Encoder
		vo.FrameInterval = p.FrameInterval
		vo.ExtractFrameCount = p.ExtractFrameCount
		vo.ExtractOutputFormat = p.ExtractOutputFormat
		vo.SpriteColumns = p.SpriteColumns
		vo.SpriteRows = p.SpriteRows
		vo.SpriteScale = p.SpriteScale
		vo.ImageTargetFormat = p.ImageTargetFormat
		vo.ImageQuality = p.ImageQuality
		vo.ImageResize = p.ImageResize
	}
	return vo
}

func (s *StrategyService) DeleteStrategy(strategyID string) error {
	first, err := s.repo.GetFirstByStrategyName(strategyID)
	if err != nil {
		return err
	}
	return s.repo.DeleteByRootID(first.RootID)
}
