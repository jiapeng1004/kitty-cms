// Package cancel 提供基于 Redis 的分布式任务取消：键 + 发布订阅，与 Java TaskCancellationRegistry 对齐。
package cancel

import (
	"context"
	"sync"
	"time"

	"github.com/redis/go-redis/v9"
)

const (
	CancelKeyPrefix = "transcoder:cancel:"
	CancelChannel   = "transcoder:cancel:channel"
	CancelTTL       = 24 * time.Hour
)

// Registry 任务取消注册表：本地 set + Redis 键/订阅；未配置 Redis 时仅本机有效。
type Registry struct {
	rdb              *redis.Client
	cancelledLocally map[string]struct{}
	runningOnThis    map[string]struct{}
	mu               sync.RWMutex
	subscribeDone    chan struct{}
	subscribeCancel  context.CancelFunc
}

// NewRegistry 创建注册表。rdb 为 nil 时仅使用本地 map（单机取消）。
func NewRegistry(rdb *redis.Client) *Registry {
	r := &Registry{
		rdb:              rdb,
		cancelledLocally: make(map[string]struct{}),
		runningOnThis:    make(map[string]struct{}),
		subscribeDone:    make(chan struct{}),
	}
	if rdb != nil {
		ctx, cancel := context.WithCancel(context.Background())
		r.subscribeCancel = cancel
		go r.subscribe(ctx)
	}
	return r
}

func (r *Registry) subscribe(ctx context.Context) {
	defer close(r.subscribeDone)
	if r.rdb == nil {
		return
	}
	pubsub := r.rdb.Subscribe(ctx, CancelChannel)
	defer pubsub.Close()
	ch := pubsub.Channel()
	for {
		select {
		case <-ctx.Done():
			return
		case msg, ok := <-ch:
			if !ok {
				return
			}
			if msg != nil && msg.Payload != "" {
				r.mu.Lock()
				r.cancelledLocally[msg.Payload] = struct{}{}
				r.mu.Unlock()
			}
		}
	}
}

// RegisterRunning 标记本节点开始执行该任务（结束时需 Clear）。
func (r *Registry) RegisterRunning(taskID string) {
	if taskID == "" {
		return
	}
	r.mu.Lock()
	defer r.mu.Unlock()
	r.runningOnThis[taskID] = struct{}{}
}

// MarkCancelled 标记任务已取消：写 Redis 键；若为本节点正在执行则写本地，否则发布到 channel。
func (r *Registry) MarkCancelled(taskID string) {
	if taskID == "" {
		return
	}
	if r.rdb != nil {
		ctx, _ := context.WithTimeout(context.Background(), 3*time.Second)
		key := CancelKeyPrefix + taskID
		_ = r.rdb.Set(ctx, key, "1", CancelTTL).Err()
	}
	r.mu.Lock()
	defer r.mu.Unlock()
	if _, running := r.runningOnThis[taskID]; running {
		r.cancelledLocally[taskID] = struct{}{}
	} else if r.rdb != nil {
		ctx, _ := context.WithTimeout(context.Background(), 2*time.Second)
		_ = r.rdb.Publish(ctx, CancelChannel, taskID).Err()
	} else {
		r.cancelledLocally[taskID] = struct{}{}
	}
}

// IsCancelled 是否已取消：先查本地，若为本节点正在执行则不再查 Redis；否则查 Redis 键。
func (r *Registry) IsCancelled(taskID string) bool {
	if taskID == "" {
		return false
	}
	r.mu.RLock()
	if _, ok := r.cancelledLocally[taskID]; ok {
		r.mu.RUnlock()
		return true
	}
	running := false
	if _, ok := r.runningOnThis[taskID]; ok {
		running = true
	}
	r.mu.RUnlock()
	if running {
		return false
	}
	if r.rdb != nil {
		ctx, _ := context.WithTimeout(context.Background(), 500*time.Millisecond)
		n, _ := r.rdb.Exists(ctx, CancelKeyPrefix+taskID).Result()
		if n > 0 {
			return true
		}
	}
	return false
}

// Clear 任务结束时清理：移除本地标记与 Redis 键。
func (r *Registry) Clear(taskID string) {
	if taskID == "" {
		return
	}
	r.mu.Lock()
	delete(r.runningOnThis, taskID)
	delete(r.cancelledLocally, taskID)
	r.mu.Unlock()
	if r.rdb != nil {
		ctx, _ := context.WithTimeout(context.Background(), 2*time.Second)
		_ = r.rdb.Del(ctx, CancelKeyPrefix+taskID).Err()
	}
}

// Close 停止订阅并释放资源。
func (r *Registry) Close() {
	if r.subscribeCancel != nil {
		r.subscribeCancel()
		<-r.subscribeDone
	}
}
