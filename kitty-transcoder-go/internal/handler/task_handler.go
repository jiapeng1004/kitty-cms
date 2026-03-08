package handler

import (
	"context"
	"encoding/json"
	"time"

	"kitty-transcoder-go/internal/broadcast"
	"kitty-transcoder-go/internal/model"
	"kitty-transcoder-go/internal/service"

	"github.com/gogf/gf/v2/frame/g"
	"github.com/gogf/gf/v2/net/ghttp"
)

type TaskHandler struct {
	svc         *service.TaskService
	broadcaster *broadcast.ProgressBroadcaster
}

func NewTaskHandler(svc *service.TaskService, broadcaster *broadcast.ProgressBroadcaster) *TaskHandler {
	return &TaskHandler{svc: svc, broadcaster: broadcaster}
}

type CreateTaskReq struct {
	g.Meta `path:"/api/transcode/task" method:"post"`
	model.CreateTaskReq
}

type CreateTaskRes struct {
	Id string `json:"id"`
}

func (h *TaskHandler) Create(ctx context.Context, req *CreateTaskReq) (res *CreateTaskRes, err error) {
	task, err := h.svc.CreateTask(&req.CreateTaskReq)
	if err != nil {
		return nil, err
	}
	return &CreateTaskRes{Id: task.ID}, nil
}

type GetTaskReq struct {
	g.Meta `path:"/api/transcode/task/{id}" method:"get"`
	Id     string `json:"id" in:"path"`
}

func (h *TaskHandler) Get(ctx context.Context, req *GetTaskReq) (res *model.TranscodeTask, err error) {
	task, err := h.svc.GetTask(req.Id)
	if err != nil {
		return nil, err
	}
	return task, nil
}

type ListTasksReq struct {
	g.Meta   `path:"/api/transcode/tasks" method:"get"`
	Page     int    `json:"page" d:"1"`
	PageSize int    `json:"pageSize" d:"20"`
	Status   string `json:"status"`
	TaskType string `json:"taskType"`
}

type ListTasksRes struct {
	List  []model.TranscodeTask `json:"list"`
	Total int64                 `json:"total"`
}

func (h *TaskHandler) List(ctx context.Context, req *ListTasksReq) (res *ListTasksRes, err error) {
	list, total, err := h.svc.ListTasks(req.Page, req.PageSize, req.Status, req.TaskType)
	if err != nil {
		return nil, err
	}
	return &ListTasksRes{List: list, Total: total}, nil
}

type CancelTaskReq struct {
	g.Meta `path:"/api/transcode/task/{id}" method:"delete"`
	Id     string `json:"id" in:"path"`
}

func (h *TaskHandler) Cancel(ctx context.Context, req *CancelTaskReq) (res *struct{}, err error) {
	return &struct{}{}, h.svc.CancelTask(req.Id)
}

type GetProgressReq struct {
	g.Meta `path:"/api/transcode/progress/{id}" method:"get"`
	Id     string `json:"id" in:"path"`
}

type ProgressRes struct {
	TaskId   string `json:"taskId"`
	Progress int    `json:"progress"`
	Status   string `json:"status"`
}

func (h *TaskHandler) Progress(ctx context.Context, req *GetProgressReq) (res *ProgressRes, err error) {
	task, err := h.svc.GetTask(req.Id)
	if err != nil {
		return nil, err
	}
	return &ProgressRes{
		TaskId:   task.ID,
		Progress: task.Progress,
		Status:   task.Status,
	}, nil
}

func (h *TaskHandler) Register(group *ghttp.RouterGroup) {
	group.Bind(
	// 需区分同一 path 的 post/get/delete，用自定义路由
	)
	group.POST("/task", h.createWrap)
	group.GET("/task/:id", h.getWrap)
	group.DELETE("/task/:id", h.cancelWrap)
	group.GET("/tasks", h.listWrap)
	group.GET("/progress/:id", h.progressWrap)
	group.GET("/sse/:id", h.sseProgressWrap)
	group.GET("/progress/stream", h.progressStreamWrap)
}

func (h *TaskHandler) createWrap(r *ghttp.Request) {
	var req model.CreateTaskReq
	if err := r.Parse(&req); err != nil {
		r.Response.WriteStatus(400)
		r.Response.Header().Set("Content-Type", "application/json; charset=UTF-8")
		r.Response.WriteJson(g.Map{"error": err.Error()})
		return
	}
	task, err := h.svc.CreateTask(&req)
	if err != nil {
		r.Response.WriteStatus(500)
		r.Response.Header().Set("Content-Type", "application/json; charset=UTF-8")
		r.Response.WriteJson(g.Map{"error": err.Error()})
		return
	}
	go func() { _ = h.svc.ProcessTask(context.Background(), task.ID) }()
	// Java createTask 返回 String（任务 ID），无包装
	r.Response.WriteJson(task.ID)
}

func (h *TaskHandler) getWrap(r *ghttp.Request) {
	id := r.Get("id").String()
	vo, err := h.svc.GetTaskVO(id)
	if err != nil {
		r.Response.WriteStatus(500)
		r.Response.Header().Set("Content-Type", "application/json; charset=UTF-8")
		r.Response.WriteJson(g.Map{"error": err.Error()})
		return
	}
	r.Response.WriteJson(vo)
}

func (h *TaskHandler) listWrap(r *ghttp.Request) {
	page := r.Get("page").Int()
	if page <= 0 {
		page = 1
	}
	pageSize := r.Get("pageSize").Int()
	if pageSize <= 0 {
		pageSize = r.Get("size").Int() // 与 Java ListTasksRequest.size 对齐
	}
	if pageSize <= 0 {
		pageSize = 20
	}
	resp, err := h.svc.ListTasksWithPagination(page, pageSize, r.Get("status").String(), r.Get("taskType").String())
	if err != nil {
		r.Response.WriteStatus(500)
		r.Response.Header().Set("Content-Type", "application/json; charset=UTF-8")
		r.Response.WriteJson(g.Map{"error": err.Error()})
		return
	}
	r.Response.WriteJson(resp)
}

func (h *TaskHandler) cancelWrap(r *ghttp.Request) {
	id := r.Get("id").String()
	if err := h.svc.CancelTask(id); err != nil {
		r.Response.WriteStatus(500)
		r.Response.Header().Set("Content-Type", "application/json; charset=UTF-8")
		r.Response.WriteJson(g.Map{"error": err.Error()})
		return
	}
	// Java cancelTask 返回 Boolean
	r.Response.WriteJson(true)
}

func (h *TaskHandler) progressWrap(r *ghttp.Request) {
	id := r.Get("id").String()
	vo, err := h.svc.GetProgressVO(id)
	if err != nil {
		r.Response.WriteStatus(500)
		r.Response.Header().Set("Content-Type", "application/json; charset=UTF-8")
		r.Response.WriteJson(g.Map{"error": err.Error()})
		return
	}
	r.Response.WriteJson(vo)
}

// sseProgressWrap GET /api/transcode/sse/:id 单任务 SSE 进度流，与 Java getProgressSSE 对齐
func (h *TaskHandler) sseProgressWrap(r *ghttp.Request) {
	id := r.Get("id").String()
	r.Response.Header().Set("Content-Type", "text/event-stream")
	r.Response.Header().Set("Cache-Control", "no-cache")
	r.Response.Header().Set("Connection", "keep-alive")
	r.Response.Flush()
	ticker := time.NewTicker(time.Second)
	defer ticker.Stop()
	for {
		select {
		case <-r.Context().Done():
			return
		case <-ticker.C:
			vo, err := h.svc.GetProgressVO(id)
			if err != nil {
				vo = &model.ProgressVO{TaskID: id, Status: "FAILED", Progress: 0}
			}
			if vo == nil {
				vo = &model.ProgressVO{TaskID: id, Status: "", Progress: 0}
			}
			writeSSEEvent(r, "progress", vo)
			r.Response.Flush()
			switch vo.Status {
			case "COMPLETED", "FAILED", "CANCELLED":
				return
			}
		}
	}
}

// progressStreamWrap GET /api/transcode/progress/stream 全任务进度广播 SSE，与 Java getProgressStream 对齐
func (h *TaskHandler) progressStreamWrap(r *ghttp.Request) {
	if h.broadcaster == nil {
		r.Response.WriteStatus(503)
		r.Response.Header().Set("Content-Type", "application/json; charset=UTF-8")
		r.Response.WriteJson(g.Map{"error": "progress stream not available"})
		return
	}
	r.Response.Header().Set("Content-Type", "text/event-stream")
	r.Response.Header().Set("Cache-Control", "no-cache")
	r.Response.Header().Set("Connection", "keep-alive")
	r.Response.Flush()
	ch := h.broadcaster.Subscribe()
	defer h.broadcaster.Unsubscribe(ch)
	for {
		select {
		case <-r.Context().Done():
			return
		case vo, ok := <-ch:
			if !ok {
				return
			}
			writeSSEEvent(r, "progress", vo)
			r.Response.Flush()
		}
	}
}

func writeSSEEvent(r *ghttp.Request, event string, data interface{}) {
	body, _ := json.Marshal(data)
	r.Response.Writef("event: %s\nid: %d\ndata: %s\n\n", event, time.Now().UnixMilli(), body)
}
