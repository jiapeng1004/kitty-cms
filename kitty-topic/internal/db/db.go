package db

import (
	"errors"
	"strings"

	"gorm.io/driver/mysql"
	"gorm.io/driver/sqlite"
	"gorm.io/gorm"
	"kitty-topic/internal/config"
)

func OpenGorm(cfg *config.Config) (*gorm.DB, error) {
	dsn := strings.TrimSpace(cfg.Database.DSN)
	if dsn == "" {
		// MVP：默认落到 sqlite 便于本地跑通（后续可替换为 MySQL）
		return gorm.Open(sqlite.Open("file:topic.db?cache=shared&_foreign_keys=1"), &gorm.Config{})
	}
	if len(dsn) < 10 {
		return nil, errors.New("database dsn too short")
	}
	return gorm.Open(mysql.Open(dsn), &gorm.Config{})
}
