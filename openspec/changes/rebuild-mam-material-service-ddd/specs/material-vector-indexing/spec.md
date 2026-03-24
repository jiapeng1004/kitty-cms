## ADDED Requirements

### Requirement: 资源向量化必须支持多来源并存
系统 MUST 支持音视频图片文本与 Office 资源向量化，并在 MySQL 与 ES 中同时保存向量数据，且一个资源 SHALL 支持多个向量来源平台并存。

#### Scenario: 写入多来源向量
- **WHEN** 同一资源分别由两个向量平台产出向量
- **THEN** 系统 MUST 保留两个来源的向量记录并可按来源条件进行检索
