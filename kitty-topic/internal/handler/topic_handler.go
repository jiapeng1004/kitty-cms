package handler

import (
	"net/http"
	"strconv"

	"kitty-topic/internal/middleware"
	"kitty-topic/internal/service"

	"github.com/gin-gonic/gin"
	"go.uber.org/zap"
)

type TopicHandler struct {
	svc  *service.TopicService
	auth gin.HandlerFunc
}

func NewTopicHandler(svc *service.TopicService, auth gin.HandlerFunc) *TopicHandler {
	return &TopicHandler{svc: svc, auth: auth}
}

type createTopicReq struct {
	Title   string `json:"title"`
	Source  string `json:"source"`
	Content string `json:"content"`
	Tags    string `json:"tags"`
}

type updateTopicReq struct {
	Title   string `json:"title"`
	Source  string `json:"source"`
	Content string `json:"content"`
	Tags    string `json:"tags"`
}

func (h *TopicHandler) Register(rg *gin.RouterGroup) {
	grp := rg.Group("/topics")
	grp.Use(h.auth)

	grp.POST("", h.create)
	grp.GET("", h.list)
	grp.GET("/:id", h.get)
	grp.PUT("/:id", h.update)
	grp.DELETE("/:id", h.delete)

	grp.POST("/:id/submit", h.submit)
	grp.POST("/:id/reject", h.reject)
	grp.POST("/:id/approve", h.approve)
	grp.POST("/:id/publish", h.publish)
}

func (h *TopicHandler) create(c *gin.Context) {
	var req createTopicReq
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"message": "invalid request"})
		return
	}
	authInfo := middleware.GetAuthInfo(c)
	userKey := ""
	if authInfo != nil {
		userKey = authInfo.UserKey
	}
	id, err := h.svc.CreateDraft(&service.CreateTopicInput{
		Title:     req.Title,
		Source:    req.Source,
		Content:   req.Content,
		Tags:      req.Tags,
		CreatedBy: userKey,
	})
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"message": err.Error()})
		return
	}
	c.JSON(http.StatusOK, gin.H{"id": id})
}

func (h *TopicHandler) list(c *gin.Context) {
	page, _ := strconv.Atoi(c.Query("page"))
	size, _ := strconv.Atoi(c.Query("size"))
	searchKey := c.Query("searchKey")
	statusStr := c.Query("status")

	var status *int
	if statusStr != "" {
		if v, err := strconv.Atoi(statusStr); err == nil {
			status = &v
		}
	}

	if page <= 0 {
		page = 1
	}
	if size <= 0 {
		size = 20
	}

	resp, err := h.svc.ListTopics(&service.ListTopicQuery{
		Page:      page,
		Size:      size,
		SearchKey: searchKey,
		Status:    status,
	})
	if err != nil {
		c.JSON(http.StatusNotImplemented, gin.H{"message": err.Error()})
		return
	}
	c.JSON(http.StatusOK, gin.H{
		"records": resp.Topics,
		"total":   resp.Total,
	})
}

func (h *TopicHandler) get(c *gin.Context) {
	id := c.Param("id")
	topic, err := h.svc.GetTopic(id)
	if err != nil {
		c.JSON(http.StatusNotImplemented, gin.H{"message": err.Error()})
		return
	}
	c.JSON(http.StatusOK, topic)
}

func (h *TopicHandler) update(c *gin.Context) {
	id := c.Param("id")
	var req updateTopicReq
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"message": "invalid request"})
		return
	}
	authInfo := middleware.GetAuthInfo(c)
	userKey := ""
	if authInfo != nil {
		userKey = authInfo.UserKey
	}

	err := h.svc.UpdateTopic(&service.UpdateTopicInput{
		ID:        id,
		Title:     req.Title,
		Source:    req.Source,
		Content:   req.Content,
		Tags:      req.Tags,
		UpdatedBy: userKey,
	})
	if err != nil {
		c.JSON(http.StatusNotImplemented, gin.H{"message": err.Error()})
		return
	}
	c.JSON(http.StatusOK, gin.H{"success": true})
}

func (h *TopicHandler) delete(c *gin.Context) {
	id := c.Param("id")
	if err := h.svc.DeleteTopic(id); err != nil {
		c.JSON(http.StatusNotImplemented, gin.H{"message": err.Error()})
		return
	}
	c.JSON(http.StatusOK, gin.H{"success": true})
}

func (h *TopicHandler) submit(c *gin.Context) {
	id := c.Param("id")
	authInfo := middleware.GetAuthInfo(c)
	userKey := ""
	if authInfo != nil {
		userKey = authInfo.UserKey
	}
	if err := h.svc.Submit(id, userKey); err != nil {
		c.JSON(http.StatusNotImplemented, gin.H{"message": err.Error()})
		return
	}
	c.JSON(http.StatusOK, gin.H{"success": true})
}

func (h *TopicHandler) reject(c *gin.Context) {
	id := c.Param("id")
	authInfo := middleware.GetAuthInfo(c)
	userKey := ""
	if authInfo != nil {
		userKey = authInfo.UserKey
	}
	if err := h.svc.Reject(id, userKey); err != nil {
		c.JSON(http.StatusNotImplemented, gin.H{"message": err.Error()})
		return
	}
	c.JSON(http.StatusOK, gin.H{"success": true})
}

func (h *TopicHandler) approve(c *gin.Context) {
	id := c.Param("id")
	authInfo := middleware.GetAuthInfo(c)
	userKey := ""
	if authInfo != nil {
		userKey = authInfo.UserKey
	}
	if err := h.svc.Approve(id, userKey); err != nil {
		c.JSON(http.StatusNotImplemented, gin.H{"message": err.Error()})
		return
	}
	c.JSON(http.StatusOK, gin.H{"success": true})
}

func (h *TopicHandler) publish(c *gin.Context) {
	id := c.Param("id")
	authInfo := middleware.GetAuthInfo(c)
	userKey := ""
	if authInfo != nil {
		userKey = authInfo.UserKey
	}
	if err := h.svc.Publish(id, userKey); err != nil {
		c.JSON(http.StatusNotImplemented, gin.H{"message": err.Error()})
		return
	}
	c.JSON(http.StatusOK, gin.H{"success": true})
}

// 使用于后续 to-do：把 handler 内的日志统一落 zap。
func (h *TopicHandler) log(logger *zap.Logger, msg string, err error) {
	if logger == nil || err == nil {
		return
	}
	logger.Error(msg, zap.Error(err))
}
