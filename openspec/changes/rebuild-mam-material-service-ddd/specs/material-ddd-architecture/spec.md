## ADDED Requirements

### Requirement: Material 服务必须遵循经典 DDD 分层
系统 MUST 将 material 能力按 `api(exchange)`、`application`、`domain`、`infra`、`repo` 分层实现，并保证依赖方向由外到内单向收敛。

#### Scenario: 模块边界校验
- **WHEN** 新增一个 material 业务用例
- **THEN** 该用例 SHALL 通过 application 编排 domain 能力，且 domain 不直接依赖 infra/repo 具体实现
