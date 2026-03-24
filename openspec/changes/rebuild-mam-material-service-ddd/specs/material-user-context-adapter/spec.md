## ADDED Requirements

### Requirement: 用户上下文能力必须通过适配接口提供
系统 MUST 定义用户上下文与权限查询端口接口，并提供基于 sa-token 与 kitty-user gRPC 插件的默认 infra 实现。

#### Scenario: 获取当前用户上下文
- **WHEN** 应用层用例请求当前登录用户信息
- **THEN** 系统 SHALL 通过用户上下文适配接口返回用户标识、角色与必要权限信息
