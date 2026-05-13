package service

import (
	"encoding/json"
	"fmt"
	"time"

	"kitty-transcoder-go/internal/model"
	"kitty-transcoder-go/internal/repository"

	"gopkg.in/yaml.v3"
	"gorm.io/gorm"
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

func (s *StrategyService) GetStrategy(strategyID int64) ([]model.TranscodeStrategyStep, error) {
	return s.repo.GetByRootID(strategyID)
}

func (s *StrategyService) ListStrategies() ([]int64, error) {
	return s.repo.ListRootIDs()
}

func (s *StrategyService) ListStrategyNames() ([]string, error) {
	return s.repo.ListStrategyNames()
}

// GetStrategyVO 返回 StrategyVO（含 steps），与 Java getStrategy 对齐
func (s *StrategyService) GetStrategyVO(strategyID int64) (*model.StrategyVO, error) {
	steps, err := s.GetStrategy(strategyID)
	if err != nil {
		return nil, err
	}
	name := ""
	if len(steps) > 0 {
		name = steps[0].StrategyName
	}
	if name == "" {
		name = fmt.Sprintf("%d", strategyID)
	}
	vo := &model.StrategyVO{
		ID:        strategyID,
		Name:      name,
		StepCount: len(steps),
		Steps:     make([]model.StrategyStepVO, 0, len(steps)),
	}
	if len(steps) > 0 && steps[0].WorkDir != "" {
		vo.WorkDir = steps[0].WorkDir
	}
	for i := range steps {
		vo.Steps = append(vo.Steps, stepToStrategyStepVO(&steps[i]))
	}
	return vo, nil
}

// ListStrategiesVO 返回策略列表（id=root_id, name, stepCount），与 Java getStrategies 对齐。
func (s *StrategyService) ListStrategiesVO() ([]model.StrategyVO, error) {
	rootIDs, err := s.repo.ListRootIDs()
	if err != nil {
		return nil, err
	}
	out := make([]model.StrategyVO, 0, len(rootIDs))
	for _, rootID := range rootIDs {
		first, _ := s.repo.GetFirstByRootID(rootID)
		stepCount := 0
		name := fmt.Sprintf("%d", rootID)
		if first != nil {
			steps, _ := s.repo.GetByRootID(rootID)
			stepCount = len(steps)
			if first.StrategyName != "" {
				name = first.StrategyName
			}
		}
		out = append(out, model.StrategyVO{
			ID:        rootID,
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

func (s *StrategyService) DeleteStrategy(strategyID int64) error {
	return s.repo.DeleteByRootID(strategyID)
}

func (s *StrategyService) ExportStrategy(strategyID int64) (string, error) {
	vo, err := s.GetStrategyVO(strategyID)
	if err != nil {
		return "", err
	}
	if vo == nil {
		return "", fmt.Errorf("strategy not found: %d", strategyID)
	}
	format := s.toExportFormat(vo)
	yamlBytes, err := yaml.Marshal(format)
	if err != nil {
		return "", fmt.Errorf("failed to marshal yaml: %w", err)
	}
	return string(yamlBytes), nil
}

func (s *StrategyService) toExportFormat(vo *model.StrategyVO) model.StrategyExportFormat {
	format := model.StrategyExportFormat{
		FormatVersion: 1,
		Name:          vo.Name,
		WorkDir:       vo.WorkDir,
		Steps:         make([]model.StrategyStepExport, 0, len(vo.Steps)),
	}
	if vo.ID != 0 {
		strategyID := fmt.Sprintf("%d", vo.ID)
		format.StrategyID = &strategyID
	}
	for _, step := range vo.Steps {
		format.Steps = append(format.Steps, s.toStepExport(&step))
	}
	return format
}

func (s *StrategyService) toStepExport(vo *model.StrategyStepVO) model.StrategyStepExport {
	export := model.StrategyStepExport{
		Type:           vo.Type,
		Depends:        vo.Depends,
		InputTemplate:  vo.InputTemplate,
		OutputTemplate: vo.OutputTemplate,
	}
	if vo.StepID != 0 {
		export.StepID = &vo.StepID
	}
	if vo.TargetFormat != "" {
		export.TargetFormat = vo.TargetFormat
	}
	if vo.Resolution != "" {
		export.Resolution = vo.Resolution
	}
	if vo.Bitrate != 0 {
		export.Bitrate = &vo.Bitrate
	}
	if vo.FrameRate != 0 {
		export.FrameRate = &vo.FrameRate
	}
	if vo.Encoder != "" {
		export.Encoder = vo.Encoder
	}
	if vo.FrameInterval != 0 {
		export.FrameInterval = &vo.FrameInterval
	}
	if vo.ExtractFrameCount != 0 {
		export.ExtractFrameCount = &vo.ExtractFrameCount
	}
	if vo.ExtractOutputFormat != "" {
		export.ExtractOutputFormat = vo.ExtractOutputFormat
	}
	if vo.SpriteColumns != 0 {
		export.SpriteColumns = &vo.SpriteColumns
	}
	if vo.SpriteRows != 0 {
		export.SpriteRows = &vo.SpriteRows
	}
	if vo.SpriteScale != 0 {
		export.SpriteScale = &vo.SpriteScale
	}
	if vo.ImageTargetFormat != "" {
		export.ImageTargetFormat = vo.ImageTargetFormat
	}
	if vo.ImageQuality != 0 {
		export.ImageQuality = &vo.ImageQuality
	}
	if vo.ImageResize != "" {
		export.ImageResize = vo.ImageResize
	}
	if vo.Condition != "" {
		export.Condition = vo.Condition
	}
	if vo.StrategyIDWhenTrue != 0 {
		export.StrategyIDWhenTrue = &vo.StrategyIDWhenTrue
	}
	if vo.StrategyIDWhenFalse != 0 {
		export.StrategyIDWhenFalse = &vo.StrategyIDWhenFalse
	}
	return export
}
