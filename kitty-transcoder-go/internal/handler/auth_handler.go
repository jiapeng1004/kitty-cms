package handler

import (
	"kitty-transcoder-go/internal/model"
	"kitty-transcoder-go/internal/service"

	"github.com/gogf/gf/v2/frame/g"
	"github.com/gogf/gf/v2/net/ghttp"
)

type AuthHandler struct {
	svc *service.AuthService
}

func NewAuthHandler(svc *service.AuthService) *AuthHandler {
	return &AuthHandler{svc: svc}
}

// Me 当前登录的 Access Key 信息（需鉴权，accessKeyId 由中间件写入）
func (h *AuthHandler) Me(r *ghttp.Request) {
	accessKeyID := r.GetCtxVar(service.AttrAccessKeyID).String()
	if accessKeyID == "" {
		r.Response.WriteStatus(401)
		r.Response.Header().Set("Content-Type", "application/json; charset=UTF-8")
		r.Response.WriteJson(g.Map{"error": "unauthorized"})
		return
	}
	vo, err := h.svc.GetAccessKey(accessKeyID)
	if err != nil {
		r.Response.WriteStatus(500)
		r.Response.Header().Set("Content-Type", "application/json; charset=UTF-8")
		r.Response.WriteJson(g.Map{"error": err.Error()})
		return
	}
	r.Response.WriteJson(vo)
}

// Login AK/SK 登录，校验通过后写 Redis 会话并返回 token
func (h *AuthHandler) Login(r *ghttp.Request) {
	var req model.LoginRequest
	if err := r.Parse(&req); err != nil {
		r.Response.WriteStatus(400)
		r.Response.Header().Set("Content-Type", "application/json; charset=UTF-8")
		r.Response.WriteJson(g.Map{"error": err.Error()})
		return
	}
	if !h.svc.ValidateAKSK(req.AccessKeyID, req.SecretKey) {
		r.Response.WriteStatus(401)
		r.Response.Header().Set("Content-Type", "application/json; charset=UTF-8")
		r.Response.WriteJson(g.Map{"error": "invalid accessKeyId or secretKey"})
		return
	}
	token, err := h.svc.GenerateLoginToken(r.Context(), req.AccessKeyID)
	if err != nil {
		r.Response.WriteStatus(500)
		r.Response.Header().Set("Content-Type", "application/json; charset=UTF-8")
		r.Response.WriteJson(g.Map{"error": err.Error()})
		return
	}
	r.Response.WriteJson(model.LoginResponse{Token: token, AccessKeyID: req.AccessKeyID})
}

// CreateAccessKey 创建 AK/SK（需鉴权）
func (h *AuthHandler) CreateAccessKey(r *ghttp.Request) {
	_ = r.GetCtxVar(service.AttrAccessKeyID).String() // 已由中间件校验
	var req model.CreateAccessKeyRequest
	if err := r.Parse(&req); err != nil {
		r.Response.WriteStatus(400)
		r.Response.Header().Set("Content-Type", "application/json; charset=UTF-8")
		r.Response.WriteJson(g.Map{"error": err.Error()})
		return
	}
	accessKeyID, secretKey, err := h.svc.GenerateAccessKey(req.Name)
	if err != nil {
		r.Response.WriteStatus(500)
		r.Response.Header().Set("Content-Type", "application/json; charset=UTF-8")
		r.Response.WriteJson(g.Map{"error": err.Error()})
		return
	}
	r.Response.WriteJson(model.CreateAccessKeyResponse{
		AccessKeyID: accessKeyID,
		SecretKey:   secretKey,
		Name:        req.Name,
	})
}

// ListAccessKeys Access Key 列表（不含 secretKey）
func (h *AuthHandler) ListAccessKeys(r *ghttp.Request) {
	list, err := h.svc.ListAccessKeys()
	if err != nil {
		r.Response.WriteStatus(500)
		r.Response.Header().Set("Content-Type", "application/json; charset=UTF-8")
		r.Response.WriteJson(g.Map{"error": err.Error()})
		return
	}
	r.Response.WriteJson(list)
}

// DeleteAccessKey 按 accessKeyId 删除
func (h *AuthHandler) DeleteAccessKey(r *ghttp.Request) {
	accessKeyID := r.Get("accessKeyId").String()
	if accessKeyID == "" {
		r.Response.WriteStatus(400)
		r.Response.Header().Set("Content-Type", "application/json; charset=UTF-8")
		r.Response.WriteJson(g.Map{"error": "accessKeyId required"})
		return
	}
	if err := h.svc.DeleteAccessKey(accessKeyID); err != nil {
		r.Response.WriteStatus(500)
		r.Response.Header().Set("Content-Type", "application/json; charset=UTF-8")
		r.Response.WriteJson(g.Map{"error": err.Error()})
		return
	}
	r.Response.WriteStatus(204)
}

func (h *AuthHandler) Register(group *ghttp.RouterGroup) {
	group.GET("/me", h.Me)
	group.POST("/login", h.Login)
	group.POST("/access-key", h.CreateAccessKey)
	group.GET("/access-key", h.ListAccessKeys)
	group.DELETE("/access-key/:accessKeyId", h.DeleteAccessKey)
}
