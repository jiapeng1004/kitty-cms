## ADDED Requirements

### Requirement: 系统必须提供 S3 兼容 Starter 供外部轻量接入
系统 MUST 以 Spring Boot Starter 形式提供 S3 兼容接入能力，使外部应用可通过标准密钥和预设配置完成资源写入与落库。

#### Scenario: 外部应用通过标准 S3 参数接入
- **WHEN** 外部应用提供 AK/SK 与目标桶配置
- **THEN** 系统 SHALL 通过 S3 兼容协议完成对象写入并生成可追踪的资源记录 ak生成或者编辑的时候会配置好必要的落库参数
