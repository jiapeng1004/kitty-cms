## Material API 权限策略（阶段1）

- 默认所有 `api` 层接口必须显式加 `@SaCheckPermission`
- 仅白名单接口可以免权限（如健康检查）
- 白名单统一登记在 `MaterialPublicEndpoints.PATHS`
- 白名单不代表匿名可写，只允许只读或探活类接口

## 当前权限码

- `material:catalog:tree:view`
- `material:resource:list:view`
