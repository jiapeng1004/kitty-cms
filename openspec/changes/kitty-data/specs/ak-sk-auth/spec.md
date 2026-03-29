## ADDED Requirements

### Requirement: AK/SK 缓存服务

系统 SHALL 启动时加载全量 AK/SK 数据到本地内存缓存，并定时刷新缓存。

#### Scenario: 应用启动加载 AK/SK
- **WHEN** 应用启动
- **THEN** 自动从 MongoDB 加载全量 AK/SK 数据到内存 Map

#### Scenario: 定时刷新缓存
- **WHEN** 距离上次刷新超过配置的间隔
- **THEN** 自动从 MongoDB 重新加载全量 AK/SK 数据替换内存缓存

#### Scenario: 手动刷新缓存
- **WHEN** 调用手动刷新接口 POST /api/v1/auth/refresh
- **THEN** 立即从 MongoDB 重新加载全量 AK/SK 数据替换内存缓存

#### Scenario: 根据 AK 获取 SK
- **WHEN** 根据传入的 AK 查询对应的 SK
- **THEN** 返回对应的 SK，若不存在则返回空

---

### Requirement: AK/SK 数据模型

系统 SHALL 定义 AK/SK 数据模型，包含以下字段：

- **ak**: Access Key，访问密钥标识
- **sk**: Secret Key，秘密密钥
- **remark**: 备注信息
- **enabled**: 是否启用
- **createTime**: 创建时间
- **updateTime**: 更新时间
