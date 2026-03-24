## ADDED Requirements

### Requirement: Material 接口必须提供干净 REST 契约
系统 SHALL 在成功场景返回无外层泛型包装的 DTO，并在失败场景返回统一异常 DTO，且请求/响应模型 MUST 定义在 exchange 层以支持 RPC 风格复用。

#### Scenario: 成功与失败返回格式一致性
- **WHEN** 客户端调用任一 material 接口
- **THEN** 成功响应 MUST 为业务 DTO，失败响应 MUST 为异常 DTO 且包含可国际化错误信息键
