## ADDED Requirements

### Requirement: 编目模板与字段绑定必须使用独立关系表
系统 MUST 使用独立绑定表维护编目模板与字段关系及排序，编目实例 SHALL 采用 EAV 结构存储资源编目值。

#### Scenario: 保存模板字段顺序
- **WHEN** 管理员为模板配置多个字段并指定顺序
- **THEN** 系统 MUST 按绑定表持久化顺序并在读取模板时按该顺序返回字段列表
