## ADDED Requirements

### Requirement: 资源检索同步必须采用事务后异步机制
系统 MUST 在资源或最新编目变更提交后投递分布式队列消息，并由消费者异步同步 ES，且消息类型 SHALL 至少支持全量文档更新与字段增量更新两类。

#### Scenario: 资源编目更新后同步 ES
- **WHEN** 资源最新编目提交成功
- **THEN** 系统 SHALL 在事务提交后发送同步消息并最终在 ES 中反映最新可检索状态
