package middleware

import (
	"crypto/hmac"
	"crypto/sha256"
	"encoding/hex"
	"net/http"
	"strconv"
	"time"

	"github.com/gin-gonic/gin"
	"github.com/kitty-cms/kitty-data/internal/config"
	"github.com/kitty-cms/kitty-data/internal/service"
	"go.uber.org/zap"
)

const (
	HeaderAK        = "X-AK"
	HeaderSign      = "X-SIGN"
	HeaderTimestamp = "X-TIMESTAMP"
)

type SignatureAuthMiddleware struct {
	akskSvc *service.AkskService
	cfg     *config.AuthConfig
	log     *zap.Logger
}

func NewSignatureAuthMiddleware(akskSvc *service.AkskService, cfg *config.AuthConfig, logger *zap.Logger) *SignatureAuthMiddleware {
	return &SignatureAuthMiddleware{
		akskSvc: akskSvc,
		cfg:     cfg,
		log:     logger,
	}
}

func (m *SignatureAuthMiddleware) Handler() gin.HandlerFunc {
	return func(c *gin.Context) {
		ak := c.GetHeader(HeaderAK)
		sign := c.GetHeader(HeaderSign)
		timestampStr := c.GetHeader(HeaderTimestamp)

		if ak == "" || sign == "" || timestampStr == "" {
			c.AbortWithStatusJSON(http.StatusUnauthorized, gin.H{"error": "missing required headers"})
			return
		}

		sk, ok := m.akskSvc.GetSK(ak)
		if !ok || sk == "" {
			c.AbortWithStatusJSON(http.StatusUnauthorized, gin.H{"error": "invalid access key"})
			return
		}

		timestamp, err := strconv.ParseInt(timestampStr, 10, 64)
		if err != nil {
			c.AbortWithStatusJSON(http.StatusBadRequest, gin.H{"error": "invalid timestamp"})
			return
		}

		driftSeconds := int64(m.cfg.TimestampDriftSeconds)
		if driftSeconds <= 0 {
			driftSeconds = 300
		}

		now := time.Now().Unix()
		if timestamp < now-driftSeconds || timestamp > now+driftSeconds {
			c.AbortWithStatusJSON(http.StatusUnauthorized, gin.H{"error": "timestamp expired"})
			return
		}

		expectedSign := ComputeSignature(sk, ak, timestamp)
		if !hmac.Equal([]byte(expectedSign), []byte(sign)) {
			c.AbortWithStatusJSON(http.StatusUnauthorized, gin.H{"error": "invalid signature"})
			return
		}

		c.Set("ak", ak)
		c.Next()
	}
}

func ComputeSignature(secretKey, ak string, timestamp int64) string {
	message := ak + strconv.FormatInt(timestamp, 10)
	h := hmac.New(sha256.New, []byte(secretKey))
	h.Write([]byte(message))
	return hex.EncodeToString(h.Sum(nil))
}
