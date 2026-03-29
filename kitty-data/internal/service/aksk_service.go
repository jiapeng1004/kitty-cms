package service

import (
	"context"
	"sync"
	"time"

	"go.uber.org/zap"

	"github.com/kitty-cms/kitty-data/internal/config"
	"github.com/kitty-cms/kitty-data/internal/repository"
)

type AkskService struct {
	repo   *repository.AkskRepo
	cache  map[string]string
	mu     sync.RWMutex
	ctx    context.Context
	cancel context.CancelFunc
	log    *zap.Logger
	cfg    *config.AuthConfig
}

func NewAkskService(repo *repository.AkskRepo, cfg *config.AuthConfig, logger *zap.Logger) *AkskService {
	ctx, cancel := context.WithCancel(context.Background())
	svc := &AkskService{
		repo:   repo,
		cache:  make(map[string]string),
		ctx:    ctx,
		cancel: cancel,
		log:    logger,
		cfg:    cfg,
	}
	return svc
}

func (s *AkskService) Start(ctx context.Context) error {
	if err := s.refresh(ctx); err != nil {
		s.log.Error("failed to load initial aksk cache", zap.Error(err))
	}

	interval := time.Duration(s.cfg.AkskRefreshIntervalSeconds) * time.Second
	if interval <= 0 {
		interval = 5 * time.Minute
	}
	s.log.Info("starting aksk refresh ticker", zap.Duration("interval", interval))

	ticker := time.NewTicker(interval)
	go func() {
		for {
			select {
			case <-ticker.C:
				if err := s.refresh(ctx); err != nil {
					s.log.Error("failed to refresh aksk cache", zap.Error(err))
				}
			case <-s.ctx.Done():
				ticker.Stop()
				s.log.Info("aksk refresh ticker stopped")
				return
			}
		}
	}()
	return nil
}

func (s *AkskService) Stop() {
	s.cancel()
}

func (s *AkskService) refresh(ctx context.Context) error {
	aksks, err := s.repo.FindAll(ctx)
	if err != nil {
		return err
	}

	s.mu.Lock()
	s.cache = make(map[string]string)
	for _, ak := range aksks {
		s.cache[ak.AK] = ak.SK
	}
	s.mu.Unlock()

	s.log.Info("aksk cache refreshed", zap.Int("count", len(aksks)))
	return nil
}

func (s *AkskService) RefreshNow(ctx context.Context) error {
	return s.refresh(ctx)
}

func (s *AkskService) GetSK(ak string) (string, bool) {
	s.mu.RLock()
	defer s.mu.RUnlock()
	sk, ok := s.cache[ak]
	return sk, ok
}

func (s *AkskService) GetAll() map[string]string {
	s.mu.RLock()
	defer s.mu.RUnlock()
	result := make(map[string]string)
	for k, v := range s.cache {
		result[k] = v
	}
	return result
}
