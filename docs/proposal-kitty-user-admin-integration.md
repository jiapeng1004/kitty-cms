# Kitty-User 前后端管理能力完善提案

## 1. 目标

将 kitty-user 后端已实现的管理接口与前端 admin 包（`frontend/packages/admin`）完整对接，并对缺失的接口在后端补齐，形成可用的租户、用户、配置与配置分类管理能力。

## 2. 现状

### 2.1 后端已有接口

| 模块 | Controller | 已有接口 |
|------|------------|----------|
| 租户 | KtTenantController | POST /api/tenant, GET /api/tenant/{id}, PUT /api/tenant/{id}, DELETE /api/tenant/{id} |
| 用户 | UserController | POST /api/user/register, POST /api/user/login, GET /api/user/captcha |
| 配置 | KtConfigController | GET /api/config/query（分页）, GET /api/config/{id}, DELETE /api/config/{id}, GET /api/config/getVal, POST /api/config/setVal |
| 配置分类 | KtConfigClassController | GET /api/configClass/query, POST /api/configClass, GET /api/configClass/{id}, DELETE /api/configClass/{id} |

### 2.2 前端现状

- **Dashboard**：用户数、配置项、分类数为硬编码，未请求后端。
- **配置管理（ConfigList.vue）**：使用 Mock 数据，未调用 `/api/config` 系列接口；表格字段与后端 `ConfigListVo` 需对齐（configName、configKey、configDesc、configWay、classId 等）。
- **配置分类（ConfigClassList.vue）**：使用 Mock 数据，未调用 `/api/configClass`；表单含「分类编码」而后端为 className/classDesc/owner，需统一。
- **租户 / 用户**：无独立管理页，需新增列表页并与后端对接。

### 2.3 缺口汇总

- **后端**
  - 配置：缺少「新增配置」「更新配置」接口（仅 setVal 可改值，无完整 CRUD 元数据）。
  - 配置分类：创建接口未使用 `@RequestBody`；缺少「更新分类」接口。
  - 租户：缺少分页列表接口（如 GET /api/tenant/query）。
  - 用户：缺少管理向接口（分页列表、按 ID 查询、更新）。
  - 仪表盘：缺少统计接口（用户数、配置项数、分类数）。

- **前端**
  - 配置管理、配置分类：改为调用真实接口，分页与表格/表单字段与后端一致。
  - 租户管理、用户管理：新增页面与路由，对接上述新接口。
  - Dashboard：调用统计接口展示实时数据。
  - 请求层：统一 baseURL、错误处理，可选增加 Token 传递。

## 3. 后端补充项（实现要点）

- **KtConfigController**
  - 类上使用 `@RequestMapping("/api/config")`（若当前未生效）。
  - 新增：`POST /api/config`（ConfigCreateDTO）、`PUT /api/config/{id}`（ConfigUpdateDTO）；DTO 与现有风格一致，入参校验。

- **KtConfigClassController**
  - `POST /api/configClass` 的 create 方法增加 `@RequestBody`。
  - 新增：`PUT /api/configClass/{id}`，入参 ClassUpdateDTO（className、classDesc、owner）。

- **租户**
  - 新增：`GET /api/tenant/query`，分页参数（page、size、searchKey 等），返回 `PageRespVo<TenantVO>`；TenantService 增加分页查询方法。

- **用户（管理端）**
  - 新增：`GET /api/user/query` 分页列表、`GET /api/user/{id}`、`PUT /api/user/{id}`；定义 UserListVO、UserPageDTO、UserUpdateDTO，与现有 DTO/VO 风格一致。

- **仪表盘统计**
  - 新增：`GET /api/dashboard/stats`（或 `/api/stats`），返回 `{ userCount, configCount, configClassCount }`，在单独 Controller 或现有某 Controller 中实现，内部调用各 Service 的 count。

## 4. 前端适配项（实现要点）

- **api.js**
  - 确认 baseURL 指向 kitty-user 服务（如 9701）；响应拦截器统一处理 401/业务错误；可选：从 localStorage 取 token 写入 Authorization。

- **配置管理（ConfigList.vue）**
  - 列表：GET /api/config/query，参数 page、size、searchKey、classId、configKey；表格列：configName、configKey、configDesc、configWay、classId、createTime 等与 ConfigListVo 一致。
  - 新增：POST /api/config；编辑：先 GET /api/config/{id}，再 PUT /api/config/{id}（或 setVal 若仅改值）；删除：DELETE /api/config/{id}。
  - 表单字段与 ConfigCreateDTO/ConfigUpdateDTO 一致（含 configWay 等）。

- **配置分类（ConfigClassList.vue）**
  - 列表：GET /api/configClass/query，分页与 searchKey、owner。
  - 新增：POST /api/configClass，Body 为 { className, classDesc, owner }，去掉「分类编码」或与后端约定一致。
  - 编辑：PUT /api/configClass/{id}；删除：DELETE /api/configClass/{id}。

- **租户管理**
  - 新增路由与菜单（如「租户管理」）；列表页调用 GET /api/tenant/query；增删改调用现有 POST/PUT/DELETE /api/tenant（及 /api/tenant/{id}）。

- **用户管理**
  - 新增路由与菜单（如「用户管理」）；列表 GET /api/user/query；详情 GET /api/user/{id}；更新 PUT /api/user/{id}。

- **Dashboard**
  - 请求 GET /api/dashboard/stats（或 /api/stats），将返回的 userCount、configCount、configClassCount 绑定到页面统计卡片。

## 5. 代码与规范

- 后端：遵循项目现有规范（DTO/VO 入参出参、禁止 Map 裸用、分页使用 PageReqDTO/PageRespVo、校验注解）。
- 前端：与现有 admin 风格一致（Ant Design Vue、表格分页、表单校验、message 提示）。

## 6. 验收

- 配置管理、配置分类、租户、用户各列表可正常分页与筛选，增删改与后端一致。
- Dashboard 三块统计为实时数据。
- 前后端接口契约与现有风格一致，无硬编码 Map 键、无裸 Map 返回。

---

## 7. 实现摘要（已做）

- **后端**：已补充配置 create/update（ConfigCreateDTO、ConfigUpdateDTO）、配置分类 @RequestBody + update（ClassUpdateDTO）、租户分页查询（TenantQueryPageDTO）、用户管理 query/getDetail/update（UserListVO、UserQueryPageDTO、UserUpdateDTO）、仪表盘 GET /api/dashboard/stats（DashboardStatsVO）。KtConfigController 已加 @RequestMapping("/api/config")。
- **前端**：配置管理、配置分类已对接真实接口；Dashboard 已对接 /api/dashboard/stats；新增租户管理、用户管理页面与路由，并加入主导航菜单。
