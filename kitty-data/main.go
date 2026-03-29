package main

import (
	"context"
	"flag"
	"fmt"
	"os"
	"os/signal"
	"syscall"

	"github.com/gin-gonic/gin"
	"go.uber.org/zap"

	"github.com/kitty-cms/kitty-data/internal/config"
	"github.com/kitty-cms/kitty-data/internal/db"
	"github.com/kitty-cms/kitty-data/internal/handler"
	"github.com/kitty-cms/kitty-data/internal/middleware"
	"github.com/kitty-cms/kitty-data/internal/repository"
	"github.com/kitty-cms/kitty-data/internal/service"
)

var (
	Version string = "0.0.1"
)

func main() {
	logger, _ := zap.NewProduction()
	defer logger.Sync()

	configPath := flag.String("config", "config.yaml", `path to config file`)

	cfg, err := config.Load(*configPath)
	if err != nil {
		logger.Fatal("failed to load config", zap.Error(err))
	}

	if err := db.Init(cfg, logger); err != nil {
		logger.Fatal("failed to init db", zap.Error(err))
	}
	defer db.Close(context.Background())

	akskRepo := repository.NewAkskRepo(cfg.Mongo.AkskCollection)
	akskSvc := service.NewAkskService(akskRepo, &cfg.Auth, logger)
	if err := akskSvc.Start(context.Background()); err != nil {
		logger.Error("failed to start aksk service", zap.Error(err))
	}
	defer akskSvc.Stop()

	eventRepo := repository.NewEventRepo(cfg.Mongo.EventsCollection)
	eventSvc := service.NewEventService(eventRepo, logger)
	sseMgr := service.NewSSEManager(logger)

	authHandler := handler.NewAuthHandler(akskSvc, logger)
	eventHandler := handler.NewEventHandler(eventSvc, sseMgr, logger)

	sigAuthMiddleware := middleware.NewSignatureAuthMiddleware(akskSvc, &cfg.Auth, logger)

	r := gin.Default()

	r.POST("/api/v1/auth/refresh", authHandler.Refresh)
	r.GET("/api/v1/auth/hello", authHandler.Hello)

	protected := r.Group("/api/v1")
	protected.Use(sigAuthMiddleware.Handler())
	{
		protected.POST("/events", eventHandler.Report)
		protected.GET("/stats/range", eventHandler.StatsRange)
		protected.GET("/stats/ranking", eventHandler.OperatorRanking)
		protected.GET("/stats/events/ranking", eventHandler.EventRanking)
		protected.GET("/stats/operator-events", eventHandler.OperatorEventRanking)
		protected.GET("/stats/trend", eventHandler.Trend)
		protected.GET("/events/monitor", eventHandler.Monitor)
	}

	addr := cfg.Server.Address
	if addr == "" {
		addr = ":9703"
	}

	go func() {
		logger.Info("starting server", zap.String("address", addr))
		if err := r.Run(addr); err != nil {
			logger.Fatal("failed to start server", zap.Error(err))
		}
	}()

	quit := make(chan os.Signal, 1)
	signal.Notify(quit, syscall.SIGINT, syscall.SIGTERM)
	<-quit

	fmt.Println("shutting down server...")
}
