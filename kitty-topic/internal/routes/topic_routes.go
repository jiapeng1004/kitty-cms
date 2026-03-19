package routes

import (
	"kitty-topic/internal/handler"

	"github.com/gin-gonic/gin"
)

func RegisterTopicRoutes(api *gin.RouterGroup, topicHandler *handler.TopicHandler) {
	topicHandler.Register(api)
}
