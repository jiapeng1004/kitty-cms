## ADDED Requirements

### Requirement: 资源模型必须采用单主键与固定父级语义
系统 SHALL 仅使用资源主键 id 作为唯一标识，资源 MUST 归属栏目且无文件夹父级时 `parentId` 固定为 `0`，资源类型 MUST 支持视频、音频、图片、文本、Office、其他、文件夹。

#### Scenario: 创建根目录资源
- **WHEN** 用户创建不在任何文件夹中的资源
- **THEN** 系统 MUST 将该资源 `parentId` 存储为 `0` 且 `catalogId` 非空
