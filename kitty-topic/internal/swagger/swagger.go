package swagger

import (
	"net/http"

	"kitty-topic/internal/config"

	"github.com/gin-gonic/gin"
	"go.uber.org/zap"
)

// MVP Swagger（占位）：
// - 后续 to-do 会补齐 swaggo 自动生成的完整 OpenAPI 文档。
// - 这里先提供一个最小的 OpenAPI JSON，确保路由存在且服务可启动。
func RegisterSwaggerRoutes(r *gin.Engine, cfg *config.Config) {
	if cfg == nil || !cfg.Server.Swagger {
		return
	}

	// 用于探测：GET /openapi.json
	r.GET("/openapi.json", func(c *gin.Context) {
		c.Header("Content-Type", "application/json; charset=UTF-8")
		_, _ = c.Writer.Write([]byte(`{"openapi":"3.0.0","info":{"title":"kitty-topic","version":"0.1.0"}}`))
	})

	// 简单 UI：GET /openapi -> 404/提示（避免引入第三方 swagger-ui 依赖）
	r.GET("/openapi", func(c *gin.Context) {
		// 如果你后续希望集成 swagger-ui，告诉我你想用的库即可。
		_ = zap.NewNop()
		c.JSON(http.StatusOK, gin.H{"message": "Swagger UI not configured yet, use /openapi.json"})
	})
}
