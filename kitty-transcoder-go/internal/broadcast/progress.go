package broadcast

import (
	"kitty-transcoder-go/internal/model"
	"sync"
)

// ProgressBroadcaster 广播任务进度到所有 SSE 订阅者，与 Java ProgressBroadcaster 对齐
type ProgressBroadcaster struct {
	mu       sync.RWMutex
	chans    []chan model.ProgressVO
	capacity int
}

func NewProgressBroadcaster(subChanCapacity int) *ProgressBroadcaster {
	if subChanCapacity <= 0 {
		subChanCapacity = 8
	}
	return &ProgressBroadcaster{capacity: subChanCapacity}
}

// Broadcast 广播一条进度，不阻塞
func (b *ProgressBroadcaster) Broadcast(vo model.ProgressVO) {
	if vo.TaskID == "" {
		return
	}
	b.mu.RLock()
	defer b.mu.RUnlock()
	for _, ch := range b.chans {
		select {
		case ch <- vo:
		default:
			// 不阻塞，跳过
		}
	}
}

// Subscribe 订阅进度流，调用方需在不用时关闭返回的 channel 或调用 Unsubscribe
func (b *ProgressBroadcaster) Subscribe() <-chan model.ProgressVO {
	ch := make(chan model.ProgressVO, b.capacity)
	b.mu.Lock()
	b.chans = append(b.chans, ch)
	b.mu.Unlock()
	return ch
}

// Unsubscribe 取消订阅
func (b *ProgressBroadcaster) Unsubscribe(ch <-chan model.ProgressVO) {
	b.mu.Lock()
	defer b.mu.Unlock()
	for i, c := range b.chans {
		if c == ch {
			b.chans = append(b.chans[:i], b.chans[i+1:]...)
			close(c)
			return
		}
	}
}
