package handler

import (
	"io"
	"net/http"
	"strconv"
	"time"

	"github.com/gin-gonic/gin"
	"github.com/kitty-cms/kitty-data/internal/model"
	"github.com/kitty-cms/kitty-data/internal/service"
	"go.uber.org/zap"
)

type EventHandler struct {
	eventSvc *service.EventService
	sseMgr   *service.SSEManager
	log      *zap.Logger
}

func NewEventHandler(eventSvc *service.EventService, sseMgr *service.SSEManager, logger *zap.Logger) *EventHandler {
	return &EventHandler{
		eventSvc: eventSvc,
		sseMgr:   sseMgr,
		log:      logger,
	}
}

func (h *EventHandler) Report(c *gin.Context) {
	var req struct {
		EventType string `json:"eventType" binding:"required"`
		Operator  string `json:"operator" binding:"required"`
		Value     int64  `json:"value" binding:"required"`
		Timestamp string `json:"timestamp"`
	}

	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	event := &model.Event{
		EventType: req.EventType,
		Operator:  req.Operator,
		Value:     req.Value,
	}

	if req.Timestamp != "" {
		if t, err := time.Parse(time.RFC3339, req.Timestamp); err == nil {
			event.Timestamp = t
		}
	}

	if err := h.eventSvc.Report(c.Request.Context(), event); err != nil {
		h.log.Error("failed to report event", zap.Error(err))
		c.JSON(http.StatusInternalServerError, gin.H{"error": "failed to report event"})
		return
	}
	h.sseMgr.Publish(event)
	c.Status(http.StatusOK)
}

func (h *EventHandler) StatsRange(c *gin.Context) {
	eventTypes := c.QueryArray("eventTypes")
	if len(eventTypes) == 0 {
		eventTypes = c.QueryArray("eventType")
	}
	startTimeStr := c.DefaultQuery("startTime", time.Now().Add(-24*time.Hour).Format(time.RFC3339))
	endTimeStr := c.DefaultQuery("endTime", time.Now().Format(time.RFC3339))

	startTime, _ := time.Parse(time.RFC3339, startTimeStr)
	endTime, _ := time.Parse(time.RFC3339, endTimeStr)

	results, err := h.eventSvc.GetStatsByTimeRange(c.Request.Context(), eventTypes, startTime, endTime)
	if err != nil {
		h.log.Error("failed to get stats", zap.Error(err))
		c.JSON(http.StatusInternalServerError, gin.H{"error": "failed to get stats"})
		return
	}

	c.JSON(http.StatusOK, results)
}

func (h *EventHandler) OperatorRanking(c *gin.Context) {
	eventTypes := c.QueryArray("eventTypes")
	if len(eventTypes) == 0 {
		eventTypes = c.QueryArray("eventType")
	}
	startTimeStr := c.DefaultQuery("startTime", time.Now().Add(-24*time.Hour).Format(time.RFC3339))
	endTimeStr := c.DefaultQuery("endTime", time.Now().Format(time.RFC3339))
	sortBy := c.DefaultQuery("sortBy", "count")
	limitStr := c.DefaultQuery("limit", "10")
	limit, _ := strconv.Atoi(limitStr)

	startTime, _ := time.Parse(time.RFC3339, startTimeStr)
	endTime, _ := time.Parse(time.RFC3339, endTimeStr)

	results, err := h.eventSvc.GetOperatorRanking(c.Request.Context(), eventTypes, startTime, endTime, sortBy, limit)
	if err != nil {
		h.log.Error("failed to get operator ranking", zap.Error(err))
		c.JSON(http.StatusInternalServerError, gin.H{"error": "failed to get ranking"})
		return
	}

	c.JSON(http.StatusOK, results)
}

func (h *EventHandler) EventRanking(c *gin.Context) {
	eventTypes := c.QueryArray("eventTypes")
	startTimeStr := c.DefaultQuery("startTime", time.Now().Add(-24*time.Hour).Format(time.RFC3339))
	endTimeStr := c.DefaultQuery("endTime", time.Now().Format(time.RFC3339))
	sortBy := c.DefaultQuery("sortBy", "count")

	startTime, _ := time.Parse(time.RFC3339, startTimeStr)
	endTime, _ := time.Parse(time.RFC3339, endTimeStr)

	results, err := h.eventSvc.GetEventRanking(c.Request.Context(), eventTypes, startTime, endTime, sortBy)
	if err != nil {
		h.log.Error("failed to get event ranking", zap.Error(err))
		c.JSON(http.StatusInternalServerError, gin.H{"error": "failed to get ranking"})
		return
	}

	c.JSON(http.StatusOK, results)
}

func (h *EventHandler) OperatorEventRanking(c *gin.Context) {
	operators := c.QueryArray("operators")
	eventTypes := c.QueryArray("eventTypes")
	startTimeStr := c.DefaultQuery("startTime", time.Now().Add(-24*time.Hour).Format(time.RFC3339))
	endTimeStr := c.DefaultQuery("endTime", time.Now().Format(time.RFC3339))

	startTime, _ := time.Parse(time.RFC3339, startTimeStr)
	endTime, _ := time.Parse(time.RFC3339, endTimeStr)

	results, err := h.eventSvc.GetOperatorEventRanking(c.Request.Context(), operators, eventTypes, startTime, endTime)
	if err != nil {
		h.log.Error("failed to get operator event ranking", zap.Error(err))
		c.JSON(http.StatusInternalServerError, gin.H{"error": "failed to get ranking"})
		return
	}

	c.JSON(http.StatusOK, results)
}

func (h *EventHandler) Trend(c *gin.Context) {
	eventTypes := c.QueryArray("eventTypes")
	startTimeStr := c.DefaultQuery("startTime", time.Now().Add(-24*time.Hour).Format(time.RFC3339))
	endTimeStr := c.DefaultQuery("endTime", time.Now().Format(time.RFC3339))
	groupBy := c.DefaultQuery("groupBy", "day")

	startTime, _ := time.Parse(time.RFC3339, startTimeStr)
	endTime, _ := time.Parse(time.RFC3339, endTimeStr)

	results, err := h.eventSvc.GetTrend(c.Request.Context(), eventTypes, startTime, endTime, groupBy)
	if err != nil {
		h.log.Error("failed to get trend", zap.Error(err))
		c.JSON(http.StatusInternalServerError, gin.H{"error": "failed to get trend"})
		return
	}

	c.JSON(http.StatusOK, results)
}

func (h *EventHandler) Monitor(c *gin.Context) {
	eventType := c.Query("eventType")
	operator := c.Query("operator")

	ch := h.sseMgr.Subscribe(eventType, operator)
	defer h.sseMgr.Unsubscribe(ch)

	c.Header("Content-Type", "text/event-stream")
	c.Header("Cache-Control", "no-cache")
	c.Header("Connection", "keep-alive")
	c.Header("Access-Control-Allow-Origin", "*")

	c.Stream(func(w io.Writer) bool {
		select {
		case data := <-ch:
			c.SSEvent("event", data)
			return true
		case <-c.Request.Context().Done():
			return false
		}
	})
}
