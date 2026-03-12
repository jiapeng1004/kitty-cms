// Package db 提供轻量事务封装，与 gormify 的 DbProvider 思路一致：
// 在需要时开启事务、在回调内执行多步写操作，避免在 engine 或业务里散落裸 SQL/裸 DB。
package db

import (
	"context"

	"gorm.io/gorm"
)

// WithTx 在事务中执行 fn，成功则提交，返回 error 则回滚。
// 用于 service 层将“更新状态 + 更新输出”等多步写放在同一事务中。
func WithTx(ctx context.Context, gormDB *gorm.DB, fn func(tx *gorm.DB) error) error {
	return gormDB.WithContext(ctx).Transaction(fn)
}
