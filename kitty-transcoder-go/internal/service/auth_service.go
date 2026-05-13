package service

import (
	"context"
	"fmt"
	"strings"
	"time"

	"github.com/google/uuid"
	"github.com/redis/go-redis/v9"
	"gorm.io/gorm"

	"kitty-transcoder-go/internal/model"
	"kitty-transcoder-go/internal/repository"
)

const (
	SessionKeyPrefix = "transcode:session:token:"
	AttrAccessKeyID  = "transcode.accessKeyId"
)

type AuthService struct {
	db         *gorm.DB
	rdb        *redis.Client
	akRepo     *repository.AccessKeyRepo
	sessionTTL time.Duration
}

func NewAuthService(db *gorm.DB, rdb *redis.Client, akRepo *repository.AccessKeyRepo, sessionTTLSeconds int) *AuthService {
	if sessionTTLSeconds <= 0 {
		sessionTTLSeconds = 86400
	}
	return &AuthService{
		db:         db,
		rdb:        rdb,
		akRepo:     akRepo,
		sessionTTL: time.Duration(sessionTTLSeconds) * time.Second,
	}
}

// GenerateAccessKey 生成 AK/SK 并落库，与 Java generateAccessKey 对齐
func (s *AuthService) GenerateAccessKey(name string) (accessKeyID, secretKey string, err error) {
	accessKeyID = "AK" + strings.ReplaceAll(uuid.New().String(), "-", "")
	secretKey = "SK" + strings.ReplaceAll(uuid.New().String(), "-", "")
	now := time.Now()
	ak := &model.TranscodeAccessKey{
		AccessKeyID: accessKeyID,
		SecretKey:   secretKey,
		Name:        name,
		Status:      "ACTIVE",
		CreatedAt:   now,
	}
	if err = s.akRepo.Create(ak); err != nil {
		return "", "", err
	}
	return accessKeyID, secretKey, nil
}

func (s *AuthService) GetSecretKey(accessKeyID string) (string, error) {
	ak, err := s.akRepo.GetByAccessKeyID(accessKeyID)
	if err != nil {
		return "", err
	}
	return ak.SecretKey, nil
}

// GenerateLoginToken 生成 Token 并写入 Redis 会话
func (s *AuthService) GenerateLoginToken(ctx context.Context, accessKeyID string) (token string, err error) {
	if s.rdb == nil {
		return "", fmt.Errorf("redis required for login session")
	}
	token = strings.ReplaceAll(uuid.New().String(), "-", "")
	key := SessionKeyPrefix + token
	if err = s.rdb.Set(ctx, key, accessKeyID, s.sessionTTL).Err(); err != nil {
		return "", err
	}
	return token, nil
}

// ValidateLoginToken 校验 Token，返回 accessKeyId 与是否有效
func (s *AuthService) ValidateLoginToken(ctx context.Context, token string) (accessKeyID string, ok bool) {
	if s.rdb == nil || token == "" {
		return "", false
	}
	key := SessionKeyPrefix + token
	val, err := s.rdb.Get(ctx, key).Result()
	if err == redis.Nil || err != nil {
		return "", false
	}
	return val, true
}

func (s *AuthService) ListAccessKeys() ([]model.AccessKeyVO, error) {
	list, err := s.akRepo.List()
	if err != nil {
		return nil, err
	}
	out := make([]model.AccessKeyVO, 0, len(list))
	for _, ak := range list {
		createdAt := ""
		if !ak.CreatedAt.IsZero() {
			createdAt = ak.CreatedAt.Format(time.RFC3339)
		}
		out = append(out, model.AccessKeyVO{
			AccessKeyID: ak.AccessKeyID,
			Name:        ak.Name,
			Status:      ak.Status,
			Description: ak.Description,
			CreatedAt:   createdAt,
		})
	}
	return out, nil
}

func (s *AuthService) GetAccessKey(accessKeyID string) (*model.AccessKeyVO, error) {
	ak, err := s.akRepo.GetByAccessKeyID(accessKeyID)
	if err != nil {
		return nil, err
	}
	createdAt := ""
	if !ak.CreatedAt.IsZero() {
		createdAt = ak.CreatedAt.Format(time.RFC3339)
	}
	return &model.AccessKeyVO{
		AccessKeyID: ak.AccessKeyID,
		Name:        ak.Name,
		Status:      ak.Status,
		Description: ak.Description,
		CreatedAt:   createdAt,
	}, nil
}

func (s *AuthService) DeleteAccessKey(accessKeyID string) error {
	return s.akRepo.DeleteByAccessKeyID(accessKeyID)
}

// ValidateAKSK 校验 accessKeyId + secretKey，用于登录
func (s *AuthService) ValidateAKSK(accessKeyID, secretKey string) bool {
	sk, err := s.GetSecretKey(accessKeyID)
	if err != nil {
		return false
	}
	return sk == secretKey
}
