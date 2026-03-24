## ADDED Requirements

### Requirement: 栏目权限必须支持 public 强语义
系统 MUST 支持 `role+catalog+permission+true`、`public+catalog+permission+true`、`public+catalog+permission+false` 三类权限记录语义，并保证 `public=false` 为不可绕过禁用规则。

#### Scenario: 公共禁用权限生效
- **WHEN** 某栏目某权限存在 `public=false` 记录
- **THEN** 任意角色（含管理员）MUST 均不可获得该权限且接口鉴权必须拒绝请求
