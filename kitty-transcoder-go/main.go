package main

import (
	"context"
	"log"
	"os"
	"path/filepath"

	"github.com/gogf/gf/v2/frame/g"
	"github.com/gogf/gf/v2/net/ghttp"
	"github.com/redis/go-redis/v9"
	"gorm.io/driver/mysql"
	"gorm.io/driver/sqlite"
	"gorm.io/gorm"

	"kitty-transcoder-go/internal/broadcast"
	"kitty-transcoder-go/internal/cancel"
	"kitty-transcoder-go/internal/config"
	"kitty-transcoder-go/internal/engine"
	"kitty-transcoder-go/internal/handler"
	"kitty-transcoder-go/internal/middleware"
	"kitty-transcoder-go/internal/model"
	"kitty-transcoder-go/internal/repository"
	"kitty-transcoder-go/internal/service"
)

// 确保 *repository.StrategyRepo 实现 engine.StrategyLoader
var _ engine.StrategyLoader = (*repository.StrategyRepo)(nil)

func main() {
	ctx := context.Background()
	// 配置：GoFrame 经典嵌套，按节点 Scan 到结构体
	var transcoderCfg config.Transcoder
	_ = g.Cfg().MustGet(ctx, "transcoder").Scan(&transcoderCfg)
	workDir := os.ExpandEnv(transcoderCfg.WorkDir)
	if workDir == "" {
		workDir = os.TempDir() + "/transcoder"
	}

	var dbCfg config.Database
	_ = g.Cfg().MustGet(ctx, "database").Scan(&dbCfg)
	dsn := dbCfg.Default.Link
	var db *gorm.DB
	var err error
	if dsn == "" || len(dsn) < 10 {
		db, err = gorm.Open(sqlite.Open("file:transcoder.db"), &gorm.Config{})
	} else {
		db, err = gorm.Open(mysql.Open(dsn), &gorm.Config{})
	}
	if err != nil {
		log.Fatal("db open: ", err)
	}
	if err := db.AutoMigrate(&model.TranscodeTask{}, &model.TranscodeStrategyStep{}, &model.TranscodeAccessKey{}); err != nil {
		log.Fatal("migrate: ", err)
	}

	var redisCfg config.Redis
	_ = g.Cfg().MustGet(ctx, "redis").Scan(&redisCfg)
	var rdb *redis.Client
	if redisCfg.Addr != "" {
		rdb = redis.NewClient(&redis.Options{
			Addr:     redisCfg.Addr,
			Password: redisCfg.Password,
			DB:       redisCfg.DB,
		})
		if _, err := rdb.Ping(ctx).Result(); err != nil {
			log.Printf("redis ping failed, using local-only cancel: %v", err)
			err := rdb.Close() // 关闭失败的连接
			log.Println("redis close: ", err)
			rdb = nil
		}
	}
	cancelReg := cancel.NewRegistry(rdb)
	defer func() {
		cancelReg.Close()
		if rdb != nil {
			err2 := rdb.Close() // 额外关闭（如果 Close 没做）
			log.Println("redis close: ", err2)
		}
	}()

	// 引擎与仓库、服务（引擎只依赖 StrategyLoader，不直接访问 DB）
	taskRepo := repository.NewTaskRepo(db)
	strategyRepo := repository.NewStrategyRepo(db)
	eng := engine.NewEngine(strategyRepo, workDir)
	accessKeyRepo := repository.NewAccessKeyRepo(db)
	var authCfg config.Auth
	_ = g.Cfg().MustGet(ctx, "auth").Scan(&authCfg)
	sessionTTL := authCfg.Session.TTLSeconds
	if sessionTTL <= 0 {
		sessionTTL = 86400
	}
	authSvc := service.NewAuthService(db, rdb, accessKeyRepo, sessionTTL)
	authHandler := handler.NewAuthHandler(authSvc)
	progressBroadcaster := broadcast.NewProgressBroadcaster(8)
	taskSvc := service.NewTaskService(db, taskRepo, strategyRepo, eng, &transcoderCfg, workDir, cancelReg, progressBroadcaster)
	strategySvc := service.NewStrategyService(db, strategyRepo)
	magicSvc := service.NewMagicService()
	taskHandler := handler.NewTaskHandler(taskSvc, progressBroadcaster)
	strategyHandler := handler.NewStrategyHandler(strategySvc)
	magicHandler := handler.NewMagicHandler(magicSvc, strategySvc, taskRepo, &transcoderCfg, workDir)
	previewHandler := handler.NewPreviewHandler(taskRepo, workDir)

	// GoFrame HTTP
	s := g.Server()
	s.Group("/api/auth", func(group *ghttp.RouterGroup) {
		group.Middleware(middleware.TokenAuth(authSvc))
		authHandler.Register(group)
	})
	s.Group("/api/transcode", func(group *ghttp.RouterGroup) {
		group.Middleware(middleware.TokenAuth(authSvc))
		taskHandler.Register(group)
		strategyHandler.Register(group)
		magicHandler.Register(group)
	})
	s.Group("/api/transcode", func(group *ghttp.RouterGroup) {
		previewHandler.Register(group)
	})

	// 静态资源：由环境变量 STATIC_DIR 指定（如 Docker COPY dist ./dist 后设为 /runtime/dist）
	if staticDir := os.Getenv("STATIC_DIR"); staticDir != "" {
		if abs, err := filepath.Abs(filepath.Clean(staticDir)); err == nil {
			staticDir = abs
		}
		if info, err := os.Stat(staticDir); err == nil && info.IsDir() {
			s.SetServerRoot(staticDir)
			s.SetIndexFiles([]string{"index.html", "index.htm"})
			log.Printf("static files: serving from %s", staticDir)
		}
	}
	s.Run()
}
