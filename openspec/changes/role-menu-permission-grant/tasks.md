## 1. 数据模型与仓储层改造

- [ ] 1.1 设计并创建 `kt_role_menu` 表结构（包含主键、`role_id`、`menu_id`、审计字段及唯一索引约束）
- [ ] 1.2 在 kitty-user-func 模块中新增 `KtRoleMenuRepository`/Mapper，支持按 `roleId + menuId` 查询与插入/更新
- [ ] 1.3 扩展或新增 `KtRolePermissionRepository` 方法，支持按 `roleId + pCode` 查询与插入，保证幂等

## 2. Service 层实现角色开通菜单与权限逻辑

- [ ] 2.1 新增 `RoleMenuPermissionService` 接口及实现类，定义 Super 接口与主接口所需的方法签名
- [ ] 2.2 在 Service 中实现 Super 接口逻辑：根据 `roleId`、`menuId` 读取菜单 `p_codes` 全量集合，为角色创建/更新 `kt_role_menu` 记录并补写对应的 `kt_role_permission`
- [ ] 2.3 在 Service 中实现主接口逻辑：根据前端传入的 `pCodes` 与菜单 `p_codes` 执行子集校验和必选权限校验，通过后创建/更新 `kt_role_menu` 并补写对应的 `kt_role_permission`
- [ ] 2.4 为 Service 方法增加事务控制（`@Transactional`），确保角色-菜单与角色-权限写入在同一事务中完成

## 3. Controller 与 API 契约实现

- [ ] 3.1 在合适的 Controller（如 `KtRoleMenuController` 或角色管理相关 Controller）中新增 Super 接口 `POST /api/admin/role/menu/super/grant`
- [ ] 3.2 新增主接口 `POST /api/role/menu/grant`，定义并实现 `GrantMenuRequest`、`SuperGrantMenuRequest` 等请求 DTO，与前端约定入参字段
- [ ] 3.3 为两个接口增加权限控制与安全校验（如角色/权限注解或路径级别限制），确保 Super 接口仅对运维/超级管理员开放

## 4. 菜单配置解析与必选权限支持

- [ ] 4.1 根据现有 `kt_menu.p_codes` 字段格式（逗号分隔或 JSON），实现统一的解析工具/方法，返回 `Set<String>`
- [ ] 4.2 设计并实现菜单「必选权限」的配置方式（新字段或在 `p_codes` 中增加标记），并在 Service 层读取解析为 `requiredPCodes`
- [ ] 4.3 在主接口逻辑中补充必选权限校验：校验 `requiredPCodes ⊆ selectedPCodes`，不满足时返回业务错误

## 5. 测试与回归验证

- [ ] 5.1 为 Super 接口与主接口编写单元测试，覆盖正常开通、重复调用幂等、非法权限 code、缺失必选权限等场景
- [ ] 5.2 编写集成测试或联调脚本，验证在实际数据库环境下对 `kt_role_menu` 与 `kt_role_permission` 的写入行为符合设计
- [ ] 5.3 与前端联调，确认页面勾选的权限列表与后端校验/写入行为一致，错误提示信息清晰可用

