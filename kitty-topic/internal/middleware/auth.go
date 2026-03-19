package middleware

import (
	"context"
	"crypto/sha256"
	"encoding/hex"
	"encoding/json"
	"fmt"
	"io"
	"net/http"
	"strings"
	"time"

	"kitty-topic/internal/config"
	"kitty-topic/internal/grpcclient/authpb"

	"github.com/gin-gonic/gin"
	"go.uber.org/zap"
	"google.golang.org/grpc"
	"google.golang.org/grpc/credentials/insecure"
	"google.golang.org/grpc/status"
)

type AuthInfo struct {
	UserKey      string
	AuthSource   string // "kitty-user" | "third-party"
	ProviderName string // 当 auth source 为 third-party 时
}

const (
	CtxAuthInfoKey = "authInfo"
)

// NewAuthMiddleware(MVP)：
func NewAuthMiddleware(cfg *config.Config) gin.HandlerFunc {
	var kittyValid bool
	var kittyUserClient authpb.AuthServiceClient
	var kittyConn *grpc.ClientConn

	// 先创建 gRPC 客户端连接（失败则只走第三方 userinfo）。
	if cfg != nil && strings.TrimSpace(cfg.Auth.KittyUserGrpcAddress) != "" {
		conn, err := grpc.Dial(
			cfg.Auth.KittyUserGrpcAddress,
			grpc.WithTransportCredentials(insecure.NewCredentials()),
			grpc.WithBlock(),
			grpc.WithTimeout(3*time.Second),
		)
		if err == nil {
			kittyConn = conn
			kittyUserClient = authpb.NewAuthServiceClient(conn)
		}
	}
	_ = kittyConn

	return func(c *gin.Context) {
		authHeader := c.GetHeader("Authorization")
		if strings.TrimSpace(authHeader) == "" {
			c.AbortWithStatusJSON(http.StatusUnauthorized, gin.H{"message": "missing Authorization"})
			return
		}
		if !strings.HasPrefix(authHeader, "Bearer ") {
			c.AbortWithStatusJSON(http.StatusUnauthorized, gin.H{"message": "invalid Authorization"})
			return
		}

		token := strings.TrimPrefix(authHeader, "Bearer ")
		token = strings.TrimSpace(token)
		if token == "" {
			c.AbortWithStatusJSON(http.StatusUnauthorized, gin.H{"message": "empty token"})
			return
		}

		// 1) kitty-user token 校验
		kittyValid = false
		if kittyUserClient != nil {
			resp, err := kittyUserClient.TokenIntrospection(
				context.Background(),
				&authpb.TokenIntrospectionReq{Token: token},
			)
			if err == nil && resp != nil {
				kittyValid = resp.Valid
			} else if err != nil {
				// ignore and fallback to third-party
				_ = status.Convert(err).Message()
			}
		}

		if kittyValid {
			// 当前 kitty-user gRPC 仅提供 valid(bool)，没有 token -> userId 映射能力。
			// 用 token hash 做稳定 userKey（用于 created_by/updated_by 审计字段）。
			userKey := tokenHashKey(token)
			c.Set(CtxAuthInfoKey, &AuthInfo{
				UserKey:      userKey,
				AuthSource:   "kitty-user",
				ProviderName: "",
			})
			c.Next()
			return
		}

		// 2) 三方 OAuth2 token 校验：按 provider userinfoUrl 调用
		provider := strings.TrimSpace(c.GetHeader("X-OAuth-Provider"))
		if provider == "" {
			c.AbortWithStatusJSON(http.StatusUnauthorized, gin.H{"message": "missing X-OAuth-Provider"})
			return
		}
		if cfg == nil {
			c.AbortWithStatusJSON(http.StatusUnauthorized, gin.H{"message": "config missing"})
			return
		}
		providerCfg := cfg.Topic.Providers[provider]
		if strings.TrimSpace(providerCfg.UserinfoURL) == "" {
			c.AbortWithStatusJSON(http.StatusUnauthorized, gin.H{"message": "provider not configured"})
			return
		}

		userID, userName, err := callProviderUserinfo(
			c.Request.Context(),
			&http.Client{Timeout: 5 * time.Second},
			providerCfg,
			token,
		)
		if err != nil {
			c.AbortWithStatusJSON(http.StatusUnauthorized, gin.H{"message": "third-party token invalid"})
			return
		}

		if strings.TrimSpace(userID) == "" {
			userID = tokenHashKey(token)
		}
		if strings.TrimSpace(userName) == "" {
			userName = userID
		}

		c.Set(CtxAuthInfoKey, &AuthInfo{
			UserKey:      fmt.Sprintf("%s:%s", provider, userID),
			AuthSource:   "third-party",
			ProviderName: userName,
		})
		c.Next()
	}
}

// GetAuthInfo 获取认证信息（可能为空）。
func GetAuthInfo(c *gin.Context) *AuthInfo {
	v, _ := c.Get(CtxAuthInfoKey)
	if v == nil {
		return nil
	}
	info, _ := v.(*AuthInfo)
	return info
}

// Helper：供后续更复杂的鉴权链路复用。
func logAuthDebug(logger *zap.Logger, info *AuthInfo) {
	if logger == nil || info == nil {
		return
	}
	logger.Debug("authInfo", zap.String("userKey", info.UserKey), zap.String("source", info.AuthSource))
}

func tokenHashKey(token string) string {
	sum := sha256.Sum256([]byte(token))
	return hex.EncodeToString(sum[:])
}

func callProviderUserinfo(ctx context.Context, client *http.Client, cfg config.ProviderConfig, token string) (userID string, userName string, err error) {
	if client == nil {
		return "", "", fmt.Errorf("http client nil")
	}
	method := strings.ToUpper(strings.TrimSpace(cfg.Method))
	if method == "" {
		method = http.MethodGet
	}

	req, err := http.NewRequestWithContext(ctx, method, strings.TrimSpace(cfg.UserinfoURL), nil)
	if err != nil {
		return "", "", err
	}
	req.Header.Set("Authorization", "Bearer "+token)
	req.Header.Set("Accept", "application/json")

	resp, err := client.Do(req)
	if err != nil {
		return "", "", err
	}
	defer func() { _ = resp.Body.Close() }()

	bodyBytes, err := io.ReadAll(resp.Body)
	if err != nil {
		return "", "", err
	}
	if resp.StatusCode < 200 || resp.StatusCode >= 300 {
		return "", "", fmt.Errorf("userinfo http %d: %s", resp.StatusCode, string(bodyBytes))
	}

	var data map[string]any
	if err := json.Unmarshal(bodyBytes, &data); err != nil {
		return "", "", err
	}

	// user id 字段默认优先 sub，其次 id
	idField := strings.TrimSpace(cfg.UserIDField)
	if idField == "" {
		if v, ok := data["sub"]; ok {
			userID, _ = v.(string)
		} else if v, ok := data["id"]; ok {
			userID, _ = v.(string)
		}
	} else {
		if v, ok := data[idField]; ok {
			userID, _ = v.(string)
		}
	}

	nameField := strings.TrimSpace(cfg.UserNameField)
	if nameField != "" {
		if v, ok := data[nameField]; ok {
			userName, _ = v.(string)
		}
	} else {
		// OIDC 常见字段：name / preferred_username / username
		if v, ok := data["name"]; ok {
			userName, _ = v.(string)
		} else if v, ok := data["preferred_username"]; ok {
			userName, _ = v.(string)
		} else if v, ok := data["username"]; ok {
			userName, _ = v.(string)
		}
	}

	return userID, userName, nil
}
