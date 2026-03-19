package config

import (
	"fmt"
	"os"
	"path/filepath"
	"strings"

	"github.com/spf13/viper"
	"go.uber.org/zap"
	"go.uber.org/zap/zapcore"
)

type Config struct {
	Server struct {
		Address string `mapstructure:"address"`
		Swagger bool   `mapstructure:"swagger"`
	}
	Grpc struct {
		Address string `mapstructure:"address"`
	}
	Topic struct {
		// 验证用户来源：先 kitty-user（token 自省），失败后走三方 provider。
		// userinfoUrl 的调用约定由下面 ProviderConfig 决定。
		Providers map[string]ProviderConfig `mapstructure:"providers"`
	}
	Database struct {
		DSN string `mapstructure:"dsn"`
	}
	Auth struct {
		// kitty-user grpc 地址（用于 TokenIntrospection）
		KittyUserGrpcAddress string `mapstructure:"kittyUserGrpcAddress"`
		// kitty-user gRPC 目标 service 名称固定为 AuthService
	}
	// Logging 对齐常见 Spring 习惯：level + file；未配 file 时仅控制台。
	Logging struct {
		Level   string `mapstructure:"level"`   // debug/info/warn/error，默认 info
		File    string `mapstructure:"file"`    // 空则只写控制台；非空则追加写入该路径
		Console bool   `mapstructure:"console"` // 配了 file 时是否仍输出到 stdout，默认 true
	} `mapstructure:"logging"`
}

type ProviderConfig struct {
	UserinfoURL string `mapstructure:"userinfoUrl"`
	// user id 字段名：默认优先解析 sub，其次解析 id。
	UserIDField string `mapstructure:"userIdField"`
	// user name 字段名（可选），用于拼装 created_by/updated_by（若缺失则使用 user id）。
	UserNameField string `mapstructure:"userNameField"`
	// HTTP 方法：userinfo 接口多数是 GET 或 POST，这里支持自定义（默认 GET）。
	Method string `mapstructure:"method"`
}

func Load() *Config {
	v := viper.New()

	v.SetEnvPrefix("KITTY_TOPIC")
	v.AutomaticEnv()
	v.SetEnvKeyReplacer(strings.NewReplacer(".", "_"))

	v.SetConfigName("application")
	v.SetConfigType("yml")
	v.AddConfigPath(".")
	_ = v.ReadInConfig() // missing config is allowed (use defaults + env)

	cfg := &Config{}

	v.SetDefault("server.address", "")
	v.SetDefault("server.swagger", true)
	v.SetDefault("grpc.address", "")

	v.SetDefault("database.dsn", "")

	v.SetDefault("auth.kittyUserGrpcAddress", "")

	// 若未配置，Providers 为空则仅 kitty-user token 校验可用
	v.SetDefault("topic.providers", map[string]ProviderConfig{})

	v.SetDefault("logging.level", "info")
	v.SetDefault("logging.file", "")
	v.SetDefault("logging.console", true)

	_ = v.Unmarshal(cfg)
	return cfg
}

// InitZap 按配置初始化日志：可读控制台格式，可选落盘；返回的 cleanup 在进程退出前调用以关闭日志文件。
func InitZap(cfg *Config) (*zap.Logger, func(), error) {
	level := zapcore.InfoLevel
	if cfg.Logging.Level != "" {
		if parsed, err := zapcore.ParseLevel(strings.ToLower(strings.TrimSpace(cfg.Logging.Level))); err == nil {
			level = parsed
		} else {
			return nil, func() {}, fmt.Errorf("logging.level: %w", err)
		}
	}

	// 终端可读格式（类似 Spring Boot console / Logback），便于本地与简单部署下直接阅读；非 JSON。
	encCfg := zapcore.EncoderConfig{
		TimeKey:        "time",
		LevelKey:       "level",
		NameKey:        "logger",
		CallerKey:      zapcore.OmitKey,
		FunctionKey:    zapcore.OmitKey,
		MessageKey:     "msg",
		StacktraceKey:  "stacktrace",
		LineEnding:     zapcore.DefaultLineEnding,
		EncodeLevel:    zapcore.CapitalLevelEncoder,
		EncodeTime:     zapcore.ISO8601TimeEncoder,
		EncodeDuration: zapcore.StringDurationEncoder,
		EncodeCaller:   zapcore.ShortCallerEncoder,
	}

	writers := make([]zapcore.WriteSyncer, 0, 2)
	var logFile *os.File

	useConsole := cfg.Logging.Console || cfg.Logging.File == ""
	if useConsole {
		writers = append(writers, zapcore.AddSync(zapcore.Lock(os.Stdout)))
	}

	if cfg.Logging.File != "" {
		dir := filepath.Dir(cfg.Logging.File)
		if dir != "" && dir != "." {
			if err := os.MkdirAll(dir, 0755); err != nil {
				return nil, func() {}, fmt.Errorf("logging.file mkdir: %w", err)
			}
		}
		f, err := os.OpenFile(cfg.Logging.File, os.O_CREATE|os.O_APPEND|os.O_WRONLY, 0644)
		if err != nil {
			return nil, func() {}, fmt.Errorf("logging.file open: %w", err)
		}
		logFile = f
		writers = append(writers, zapcore.AddSync(zapcore.Lock(f)))
	}

	if len(writers) == 0 {
		writers = append(writers, zapcore.AddSync(zapcore.Lock(os.Stdout)))
	}

	var out zapcore.WriteSyncer
	if len(writers) == 1 {
		out = writers[0]
	} else {
		out = zapcore.NewMultiWriteSyncer(writers...)
	}

	core := zapcore.NewCore(
		zapcore.NewConsoleEncoder(encCfg),
		out,
		level,
	)

	cleanup := func() {
		if logFile != nil {
			_ = logFile.Close()
		}
	}

	return zap.New(core).Named("kitty-topic"), cleanup, nil
}
