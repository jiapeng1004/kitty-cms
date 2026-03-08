package handler

import (
	"github.com/gogf/gf/v2/frame/g"
	"github.com/gogf/gf/v2/net/ghttp"
	"kitty-transcoder-go/internal/model"
	"kitty-transcoder-go/internal/service"
)

type StrategyHandler struct {
	svc *service.StrategyService
}

func NewStrategyHandler(svc *service.StrategyService) *StrategyHandler {
	return &StrategyHandler{svc: svc}
}

func (h *StrategyHandler) Register(group *ghttp.RouterGroup) {
	group.POST("/strategy", h.create)
	group.GET("/strategy", h.list)
	group.GET("/strategy/:id", h.get)
	group.DELETE("/strategy/:id", h.delete)
}

type CreateStrategyReq struct {
	Name  string                    `json:"name"`
	Steps []model.StrategyStepParam `json:"steps"`
}

func (h *StrategyHandler) create(r *ghttp.Request) {
	var req CreateStrategyReq
	if err := r.Parse(&req); err != nil {
		r.Response.WriteStatus(400)
		r.Response.Header().Set("Content-Type", "application/json; charset=UTF-8")
		r.Response.WriteJson(g.Map{"error": err.Error()})
		return
	}
	if req.Name == "" {
		r.Response.WriteStatus(400)
		r.Response.Header().Set("Content-Type", "application/json; charset=UTF-8")
		r.Response.WriteJson(g.Map{"error": "name required"})
		return
	}
	_, err := h.svc.CreateStrategy(req.Name, req.Steps)
	if err != nil {
		r.Response.WriteStatus(500)
		r.Response.Header().Set("Content-Type", "application/json; charset=UTF-8")
		r.Response.WriteJson(g.Map{"error": err.Error()})
		return
	}
	// Java createStrategy 返回 String（策略 ID/名称），无包装
	r.Response.WriteJson(req.Name)
}

// list GET /api/transcode/strategy 返回 List<StrategyVO>，与 Java getStrategies 一致（非 rootID 数组）
func (h *StrategyHandler) list(r *ghttp.Request) {
	list, err := h.svc.ListStrategiesVO()
	if err != nil {
		r.Response.WriteStatus(500)
		r.Response.Header().Set("Content-Type", "application/json; charset=UTF-8")
		r.Response.WriteJson(g.Map{"error": err.Error()})
		return
	}
	if list == nil {
		list = []model.StrategyVO{}
	}
	r.Response.Header().Set("Content-Type", "application/json; charset=UTF-8")
	r.Response.WriteJson(list)
}

func (h *StrategyHandler) get(r *ghttp.Request) {
	id := r.Get("id").String()
	vo, err := h.svc.GetStrategyVO(id)
	if err != nil {
		r.Response.WriteStatus(500)
		r.Response.Header().Set("Content-Type", "application/json; charset=UTF-8")
		r.Response.WriteJson(g.Map{"error": err.Error()})
		return
	}
	r.Response.WriteJson(vo)
}

func (h *StrategyHandler) delete(r *ghttp.Request) {
	id := r.Get("id").String()
	if err := h.svc.DeleteStrategy(id); err != nil {
		r.Response.WriteStatus(500)
		r.Response.Header().Set("Content-Type", "application/json; charset=UTF-8")
		r.Response.WriteJson(g.Map{"error": err.Error()})
		return
	}
	r.Response.WriteStatus(204)
}
