package main

import (
	"context"
	"fmt"
	"net"
	"net/http"
	"os"
	"time"

	"kitty-topic/internal/config"
	"kitty-topic/internal/db"
	"kitty-topic/internal/grpcserver"
	"kitty-topic/internal/handler"
	"kitty-topic/internal/middleware"
	"kitty-topic/internal/model"
	"kitty-topic/internal/repository"
	"kitty-topic/internal/routes"
	"kitty-topic/internal/service"
	"kitty-topic/internal/swagger"

	"github.com/gin-gonic/gin"
	"go.uber.org/zap"
	"google.golang.org/grpc"
	"google.golang.org/grpc/reflection"
)

func main() {
	ctx := context.Background()

	cfg := config.Load()

	logger, logCleanup, err := config.InitZap(cfg)
	if err != nil {
		fmt.Fprintf(os.Stderr, "init logger: %v\n", err)
		os.Exit(1)
	}
	defer logCleanup()
	defer func() { _ = logger.Sync() }()
	logger.Info("kitty-topic starting", zap.String("service", "kitty-topic"))

	gormDB, err := db.OpenGorm(cfg)
	if err != nil {
		logger.Fatal("db open failed", zap.Error(err))
	}
	if err := gormDB.AutoMigrate(&model.Topic{}); err != nil {
		logger.Fatal("auto-migrate failed", zap.Error(err))
	}

	// 基础仓库/服务/handler（后续 to-dos 会实现完整逻辑）
	topicRepo := repository.NewTopicRepo(gormDB)
	topicSvc := service.NewTopicService(topicRepo)

	authMiddleware := middleware.NewAuthMiddleware(cfg)
	topicHandler := handler.NewTopicHandler(topicSvc, authMiddleware)

	// Gin
	gin.SetMode(gin.ReleaseMode)
	r := gin.New()
	r.Use(gin.Recovery())
	r.Use(middleware.RequestID())
	r.Use(middleware.ZapLogger(logger))

	api := r.Group("/api")
	routes.RegisterTopicRoutes(api, topicHandler)

	// Swagger
	swagger.RegisterSwaggerRoutes(r, cfg)

	// gRPC
	grpcSrv := grpc.NewServer()
	grpcserver.RegisterTopicGrpcServer(grpcSrv, topicSvc)
	reflection.Register(grpcSrv)

	grpcAddr := cfg.Grpc.Address
	if grpcAddr == "" {
		grpcAddr = ":9091"
	}
	lis, err := net.Listen("tcp", grpcAddr)
	if err != nil {
		logger.Fatal("grpc listen failed", zap.Error(err))
	}
	go func() {
		logger.Info("grpc listening", zap.String("addr", grpcAddr))
		_ = grpcSrv.Serve(lis)
	}()

	httpAddr := cfg.Server.Address
	if httpAddr == "" {
		httpAddr = ":9704"
	}
	srv := &http.Server{
		Addr:           httpAddr,
		Handler:        r,
		ReadTimeout:    10 * time.Second,
		WriteTimeout:   10 * time.Second,
		MaxHeaderBytes: 1 << 20,
	}
	logger.Info("http listening", zap.String("addr", httpAddr))
	if err := srv.ListenAndServe(); err != nil && err != context.Canceled {
		// Gin.Server.ListenAndServe 会直接阻塞并返回错误
		logger.Fatal("http serve failed", zap.Error(err))
	}

	_ = os.Getenv("STATIC_DIR")
	_ = ctx
}
