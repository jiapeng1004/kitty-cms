package service

import (
	"encoding/json"
	"sync"

	"github.com/kitty-cms/kitty-data/internal/model"
	"go.uber.org/zap"
)

type SSEManager struct {
	mu      sync.RWMutex
	clients map[chan []byte]bool
	eventCh chan *model.Event
	log     *zap.Logger
}

func NewSSEManager(logger *zap.Logger) *SSEManager {
	mgr := &SSEManager{
		clients: make(map[chan []byte]bool),
		eventCh: make(chan *model.Event, 1000),
		log:     logger,
	}
	go mgr.broadcast()
	return mgr
}

func (m *SSEManager) broadcast() {
	for event := range m.eventCh {
		data, err := json.Marshal(event)
		if err != nil {
			m.log.Error("failed to marshal event", zap.Error(err))
			continue
		}

		m.mu.RLock()
		for ch := range m.clients {
			select {
			case ch <- data:
			default:
			}
		}
		m.mu.RUnlock()
	}
}

func (m *SSEManager) Subscribe(eventType, operator string) chan []byte {
	ch := make(chan []byte, 100)
	m.mu.Lock()
	m.clients[ch] = true
	m.mu.Unlock()
	go func() {
		for data := range ch {
			if m.shouldSend(eventType, operator, data) {
			}
		}
	}()
	return ch
}

func (m *SSEManager) shouldSend(eventType, operator string, data []byte) bool {
	var event model.Event
	if err := json.Unmarshal(data, &event); err != nil {
		return false
	}
	if eventType != "" && event.EventType != eventType {
		return false
	}
	if operator != "" && event.Operator != operator {
		return false
	}
	return true
}

func (m *SSEManager) Unsubscribe(ch chan []byte) {
	m.mu.Lock()
	if _, ok := m.clients[ch]; ok {
		delete(m.clients, ch)
		close(ch)
	}
	m.mu.Unlock()
}

func (m *SSEManager) Publish(event *model.Event) {
	select {
	case m.eventCh <- event:
	default:
		m.log.Warn("event channel full, dropping event")
	}
}
