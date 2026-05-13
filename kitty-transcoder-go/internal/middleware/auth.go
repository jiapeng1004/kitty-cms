package middleware

import (
	"strings"

	"kitty-transcoder-go/internal/service"

	"github.com/gogf/gf/v2/net/ghttp"
)

// TokenAuth 从 Authorization Bearer / X-Api-Token / query token 校验登录会话，并将 accessKeyId 写入请求上下文
// 白名单：POST /api/auth/login 不鉴权
func TokenAuth(authSvc *service.AuthService) ghttp.HandlerFunc {
	return func(r *ghttp.Request) {
		if r.Method == "POST" && (r.URL.Path == "/api/auth/login" || strings.HasSuffix(r.URL.Path, "/api/auth/login")) {
			r.Middleware.Next()
			return
		}
		token := ""
		if s := r.Header.Get("Authorization"); strings.HasPrefix(s, "Bearer ") {
			token = strings.TrimPrefix(s, "Bearer ")
		} else if token == "" {
			token = r.Header.Get("X-Api-Token")
		}
		if token == "" {
			token = r.Get("token").String()
		}
		if token == "" {
			r.Response.WriteStatus(401)
			r.Response.Header().Set("Content-Type", "application/json; charset=UTF-8")
			r.Response.WriteJson(map[string]interface{}{"error": "未认证：请使用登录 Token（Authorization: Bearer 或 X-Api-Token）或 AK/SK 签名"})
			return
		}
		accessKeyID, ok := authSvc.ValidateLoginToken(r.Context(), token)
		if !ok {
			r.Response.WriteStatus(401)
			r.Response.Header().Set("Content-Type", "application/json; charset=UTF-8")
			r.Response.WriteJson(map[string]interface{}{"error": "invalid or expired token"})
			return
		}
		r.SetCtxVar(service.AttrAccessKeyID, accessKeyID)
		r.Middleware.Next()
	}
}
