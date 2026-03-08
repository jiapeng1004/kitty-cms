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
	rootID, err := h.svc.CreateStrategy(req.Name, req.Steps)
	if err != nil {
		r.Response.WriteStatus(500)
		r.Response.Header().Set("Content-Type", "application/json; charset=UTF-8")
		r.Response.WriteJson(g.Map{"error": err.Error()})
		return
	}
	r.Response.WriteJson(g.Map{"id": req.Name, "rootId": rootID})
}

func (h *StrategyHandler) list(r *ghttp.Request) {
	ids, err := h.svc.ListStrategies()
	if err != nil {
		r.Response.WriteStatus(500)
		r.Response.Header().Set("Content-Type", "application/json; charset=UTF-8")
		r.Response.WriteJson(g.Map{"error": err.Error()})
		return
	}
	r.Response.WriteJson(ids)
}

func (h *StrategyHandler) get(r *ghttp.Request) {
	id := r.Get("id").String()
	steps, err := h.svc.GetStrategy(id)
	if err != nil {
		r.Response.WriteStatus(500)
		r.Response.Header().Set("Content-Type", "application/json; charset=UTF-8")
		r.Response.WriteJson(g.Map{"error": err.Error()})
		return
	}
	r.Response.WriteJson(steps)
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
