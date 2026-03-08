package config

// Config 根配置（GoFrame 经典嵌套结构，与 config.yaml 对应）
type Config struct {
	Server     Server     `json:"server"`
	Grpc       Grpc       `json:"grpc"`
	Database   Database   `json:"database"`
	Redis      Redis      `json:"redis"`
	Auth       Auth       `json:"auth"`
	Transcoder Transcoder `json:"transcoder"`
}

// Auth 登录会话与签名（与 Java AuthConfig 对齐）
type Auth struct {
	Session   AuthSession   `json:"session"`
	Signature AuthSignature `json:"signature"`
}

type AuthSession struct {
	TTLSeconds int `json:"ttlSeconds"` // 默认 86400
}

type AuthSignature struct {
	TimestampDriftSeconds int `json:"timestampDriftSeconds"` // 默认 300
}

// Server HTTP 服务
type Server struct {
	Address        string `json:"address"`
	OpenApiPath    string `json:"openapiPath"`
	SwaggerPath    string `json:"swaggerPath"`
	RouteOverWrite bool   `json:"routeOverWrite"`
}

// Grpc gRPC 服务
type Grpc struct {
	Address string `json:"address"`
}

// Database 数据库（GORM）
type Database struct {
	Default DatabaseNode `json:"default"`
}

type DatabaseNode struct {
	Type string `json:"type"`
	Link string `json:"link"`
}

// Redis 分布式任务取消（发布订阅）。Addr 留空则禁用 Redis
type Redis struct {
	Addr     string `json:"addr"`     // 如 "localhost:6379"
	Password string `json:"password"` // 无密码留空
	DB       int    `json:"db"`       // 库索引，默认 0
}

// Transcoder 转码工作目录与输出
type Transcoder struct {
	WorkDir string `json:"workDir"`
	TempDir string `json:"tempDir"`
	Output  Output `json:"output"`
}

type Output struct {
	HttpPrefix string `json:"httpPrefix"`
}
