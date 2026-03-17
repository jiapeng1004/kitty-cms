## Context

本设计针对「角色开通菜单及权限」能力 (`role-menu-permission-grant`)，在 kitty-cms 现有技术栈（Java + Spring Boot + R2DBC + MySQL + Vue.js）下，明确后端的接口形态、Service 设计、数据表使用方式，以及与前端的交互约定。

当前情况：
- 菜单元数据存储在 `kt_menu` 表中，其中通过字段 `p_codes` 声明与该菜单绑定的权限 code 集合。
- 角色与权限的关系存储在 `kt_role_permission` 表中。
- 缺少角色与菜单的直接绑定表，且没有统一的角色开通菜单 & 同步权限的业务入口。

目标是新增/规范：
- 角色与菜单的关系表 `kt_role_menu`。
- 一组后端 Service 与 Controller 接口，用于：
  - Super 后门接口：自动按菜单声明的全部 `p_codes` 为指定角色开通权限；
  - 主接口：根据前端回传的（默认全选但用户可取消的）权限 code 集合，为角色开通菜单，并只为勾选的 code 建立角色-权限关系。
- 清晰的校验规则，保证前端传入的权限 code 不会越权（必须为菜单声明的 `p_codes` 子集）。

## Goals / Non-Goals

**Goals:**
- 设计并落地 `kt_role_menu` 数据模型及对应的 Repository/DAO。
- 设计角色开通菜单的后端 Service 方法（区分 Super 接口与主接口），包含：
  - 角色-菜单关系的插入/更新；
  - 基于菜单 `p_codes` 及前端回传 code 集合的角色-权限关系补写。
- 设计 Controller 层接口定义（请求/响应 DTO 及路径风格），与前端形成明确契约。
- 定义主接口的权限 code 校验逻辑（`request.pCodes ⊆ menu.pCodes`，以及可选的「必选权限」规则）。

**Non-Goals:**
- 不设计/实现前端页面的 UI 细节，仅约定前后端参数与行为。
- 不改动现有通用权限校验框架（如登录态、鉴权拦截器），只在本业务内使用既有能力。
- 不处理批量角色、批量菜单在一次请求中的复杂批量开通（本次按单角色单菜单一次开通为主，后续如需批量可在本设计基础上扩展）。

## Decisions

1. **数据模型与存储层**
   - 引入新表 `kt_role_menu`：
     - 字段示例：`id`（主键）、`role_id`、`menu_id`、`created_at`、`updated_at`、`created_by` 等。
     - 约束：`role_id + menu_id` 唯一索引，避免重复绑定。
   - 复用现有表：
     - `kt_menu`：读取 `p_codes`（字符串，逗号分隔或 JSON 结构，具体以现有字段定义为准）。
     - `kt_role_permission`：插入角色-权限绑定记录。
   - 访问技术：沿用项目现有风格（例如 Spring Data R2DBC 或自定义 R2DBC Repository），新建对应的 Repository/Mapper 类：
     - `KtRoleMenuRepository`：按 `roleId + menuId` 查询与插入/更新。
     - 复用/扩展现有 `KtRolePermissionRepository`，新增按 `roleId + pCode` 查询/插入方法。

2. **Service 层接口设计**
   - 新增 `RoleMenuPermissionService`（命名可根据项目惯例微调），提供两个核心方法：
     - `grantMenuWithPermissionsSuper(roleId, menuId)`：
       - 步骤：
         1. 校验角色、菜单存在性与合法性；
         2. 创建/更新 `kt_role_menu` 中的 `(roleId, menuId)` 记录；
         3. 从 `kt_menu` 读取该菜单的全部 `p_codes` 集合；
         4. 遍历全部 `p_codes`，对每个 code：
            - 查询 `kt_role_permission` 是否已有 `(roleId, code)` 记录；
            - 若不存在，则插入一条新记录。
     - `grantMenuWithPermissions(roleId, menuId, selectedPCodes)`（主接口）：
       - 步骤：
         1. 校验角色、菜单存在性与合法性；
         2. 从 `kt_menu` 读取该菜单的全部 `p_codes` 集合，得到 `menuPCodes`；
         3. 对前端传入的 `selectedPCodes`：
            - 校验 `selectedPCodes ⊆ menuPCodes`，否则抛出业务异常；
            - 如有「必选权限」配置，则校验 `selectedPCodes` 是否满足必选项（例如：`requiredPCodes ⊆ selectedPCodes`），不满足则抛出业务异常；
         3. 创建/更新 `kt_role_menu` 中的 `(roleId, menuId)` 记录；
         4. 遍历 `selectedPCodes`，对每个 code：
            - 查询 `kt_role_permission` 是否已有 `(roleId, code)` 记录；
            - 若不存在，则插入一条新记录。
   - 两个方法内部共用一套「根据角色、菜单和目标 code 集合补写 `kt_role_permission`」的私有辅助方法，以减少重复逻辑。

3. **Controller 层接口设计**
   - 命名空间建议按现有模式放入如 `KtRoleMenuController` 或权限/角色相关 Controller 中，路径示例：
     - Super 接口（后门，仅运维可用，可增加权限/环境限制）：
       - `POST /api/admin/role/menu/super/grant`
       - 请求体 DTO：`SuperGrantMenuRequest { Long roleId; Long menuId; }`
     - 主接口（页面使用）：
       - `POST /api/role/menu/grant`
       - 请求体 DTO：`GrantMenuRequest { Long roleId; Long menuId; List<String> pCodes; }`
   - 响应建议统一使用项目已有的封装（如 `ApiResponse<Void>` 或类似结构），返回操作是否成功及必要的错误码/错误信息。
   - 安全控制：
     - Super 接口仅允许拥有高权限（例如超级管理员、运维角色）的用户调用，可通过角色/权限注解或接口前缀区分。
     - 主接口则根据系统权限模型，对角色管理页面可操作的管理员开放。

4. **菜单 `p_codes` 解析与「必选权限」扩展**
   - `p_codes` 字段的解析：
     - 若为逗号分隔字符串，例如 `"article:view,article:edit"`，则在 Service 中拆分为 `Set<String>`；
     - 若为 JSON 存储（如 `["article:view","article:edit"]`），则使用项目通用的 JSON 工具反序列化。
   - 「必选权限」的设计预留：
     - 可在 `kt_menu` 表新增字段 `required_p_codes`，或在 `p_codes` 结构中标识必选项（例如 `view*` 表示必选），本设计先以「如有配置则校验」为前置，具体落地方式在后续表结构设计中细化。
     - Service 逻辑仅需在校验阶段多一步：计算 `requiredPCodes`，并校验 `requiredPCodes ⊆ selectedPCodes`。

5. **幂等性与事务边界**
   - 幂等性：
     - `kt_role_menu` 通过 `role_id + menu_id` 唯一约束保证幂等；在 Service 层统一使用「存在则更新，不存在则插入」方式处理。
     - `kt_role_permission` 在插入前先查询是否存在同一 `(roleId, pCode)` 记录，已存在则跳过，避免主键/唯一索引冲突。
   - 事务控制：
     - 每次角色开通菜单（无论 Super 还是主接口）应处于单个事务中：
       - 成功则同时写入 `kt_role_menu` 和对应多条 `kt_role_permission`；
       - 失败则整体回滚，避免「菜单已开通但权限未齐全」的不一致状态。
     - 采用 Spring 事务（`@Transactional`）注解在 Service 方法上管理。

## Risks / Trade-offs

- **风险：菜单 `p_codes` 配置错误或与前端展示不一致**
  - 若 `kt_menu.p_codes` 配置与前端使用的权限枚举不一致，可能导致前端提示的权限与实际可开通权限不匹配。
  - 缓解：建立菜单配置管理规范，前端从后端接口获取菜单及其 `p_codes`，避免在前端硬编码。

- **风险：Super 接口误用导致权限过大**
  - Super 接口会自动开通菜单声明的全部权限，一旦被非预期角色调用，可能带来权限扩大风险。
  - 缓解：限制接口路径、权限注解与网关/防火墙配置，仅开放给运维/超级管理员；必要时可在配置中开关该接口。

- **权衡：主接口只对勾选的 code 建立角色-权限关系**
  - 选择由前端提示并默认勾选所有 `p_codes`，管理员可取消，从而允许灵活控制权限范围。
  - 权衡在于：管理员可能忘记勾选某些推荐权限，导致功能不可用。
  - 缓解：利用「必选权限」机制，强制某些关键权限必须包含在勾选集合中。

- **性能与批量操作**
  - 一次开通菜单可能对应较多 `p_codes`，逐条查询/插入 `kt_role_permission` 存在一定性能开销。
  - 当前以「单菜单、单角色」为粒度，预估数据量可接受；后续如有大量批量场景，可引入批量查询和批量插入优化。

