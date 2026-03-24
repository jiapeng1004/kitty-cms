## ADDED Requirements

### Requirement: 站内信必须支持 SSE 实时推送与跨 Pod 协调
系统 MUST 支持用户间站内消息发送与状态管理，并通过 SSE 实时推送消息；在多 Pod 部署下 MUST 通过分布式队列协调消息分发。

#### Scenario: 接收者连接在不同 Pod
- **WHEN** 发送者与接收者连接到不同应用实例
- **THEN** 系统 SHALL 通过队列转发并确保接收者 SSE 通道可收到实时消息事件
