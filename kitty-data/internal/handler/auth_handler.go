package handler

import (
	"net/http"

	"github.com/gin-gonic/gin"
	"github.com/kitty-cms/kitty-data/internal/service"
	"go.uber.org/zap"
)

type AuthHandler struct {
	akskSvc *service.AkskService
	log     *zap.Logger
}

func NewAuthHandler(akskSvc *service.AkskService, logger *zap.Logger) *AuthHandler {
	return &AuthHandler{
		akskSvc: akskSvc,
		log:     logger,
	}
}

func (h *AuthHandler) Refresh(c *gin.Context) {
	if err := h.akskSvc.RefreshNow(c.Request.Context()); err != nil {
		h.log.Error("failed to refresh aksk cache", zap.Error(err))
		c.JSON(http.StatusInternalServerError, gin.H{"error": "refresh failed"})
		return
	}
	h.log.Info("aksk cache refreshed manually")
	c.Status(http.StatusOK)
}

func (h *AuthHandler) Hello(c *gin.Context) {
	c.Status(http.StatusOK)
}
