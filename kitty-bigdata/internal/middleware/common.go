package middleware

import (
	"time"

	"github.com/gin-gonic/gin"
	"github.com/google/uuid"
	"go.uber.org/zap"
)

func RequestID() gin.HandlerFunc {
	return func(c *gin.Context) {
		rid := uuid.NewString()
		c.Set("request_id", rid)
		c.Writer.Header().Set("X-Request-Id", rid)
		c.Next()
	}
}

func ZapLogger(logger *zap.Logger) gin.HandlerFunc {
	return func(c *gin.Context) {
		start := time.Now()
		path := c.Request.URL.Path
		method := c.Request.Method

		c.Next()

		fields := []zap.Field{
			zap.String("request_id", c.GetString("request_id")),
			zap.String("method", method),
			zap.String("path", path),
			zap.Int("status", c.Writer.Status()),
			zap.Duration("cost", time.Since(start)),
		}
		if logger != nil {
			logger.Info("http request", fields...)
		}
	}
}
