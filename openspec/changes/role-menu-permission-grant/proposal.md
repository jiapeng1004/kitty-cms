# 角色开通菜单及权限 - OpenSpec 提案

## Why

角色开通菜单时，需要同时维护「角色-菜单」关系和「角色-权限」关系。当前缺乏统一的业务逻辑和接口规范：菜单在 `kt_menu` 中通过 `p_codes` 声明与该菜单绑定的权限 code，但：
- Super 场景是否自动开通全部声明的权限 code？
- 页面实际开通时，前端如何提示/默认勾选这些 code？
- 管理员是否可以在页面上取消部分乃至全部 code 的勾选？
- 后端如何校验前端回传的权限 code 是否在菜单允许同步的范围内？

需要通过 OpenSpec 明确：Super 接口作为后门入口，默认按菜单声明的全部权限 code 自动开通；正式页面使用的主接口则由前端提示并默认全选菜单声明的权限 code，允许管理员取消部分或全部勾选，后端再对前端回传的权限 code 做严格校验并同步写入 `kt_role_permission`。

## What Changes

- **新增角色-菜单关系表 `kt_role_menu`**：存储角色与菜单的绑定关系（role_id, menu_id）
- **双接口设计**：
  - **Super 接口（后门）**：仅供运维/调试使用，不在正常页面暴露；开通菜单时不依赖前端勾选，直接按菜单 `p_codes` 声明的全部权限 code 进行开通
  - **主接口（页面使用）**：前端根据菜单 `p_codes` 提示需同步开通的权限列表，并在 UI 上默认全选；管理员在操作时可以取消其中的某些或全部勾选；前端将最终选中的权限 code 列表一并回传后端
- **后端 Service 逻辑**：
  - 在 `kt_role_menu` 中插入/更新角色-菜单关系
  - 读取菜单配置中的 `p_codes` 作为该菜单声明的可同步权限集合
  - 对 Super 接口：以菜单 `p_codes` 全量为准，针对每个 code，若 `kt_role_permission` 中不存在该角色-权限关系，则补一条
  - 对主接口：仅对前端回传的权限 code 列表进行处理，针对每个 code，若 `kt_role_permission` 中不存在该角色-权限关系，则补一条
- **主接口校验规则**：
  - 后端必须校验「前端传入的权限 code 集合」是「菜单声明的 `p_codes` 集合」的子集，否则拒绝请求
  - 若菜单声明了「必选权限」概念，则后端应在此基础上进一步保证：被前端回传的 code 集合中不得包含菜单未声明的权限，同时可以根据业务需要校验是否必须包含全部必选项

## Capabilities

### New Capabilities
- `role-menu-permission-grant`: 角色开通菜单及权限的业务逻辑，包括 Super 接口（自动勾选菜单全部权限）、主接口（前端回传选中权限并校验）、`kt_role_menu` 与 `kt_role_permission` 的维护

### Modified Capabilities
- 无

## Impact

- **数据库**：新增 `kt_role_menu` 表（role_id, menu_id 等字段）
- **后端**：kitty-user-func 中新增/扩展 Service 方法、Controller 接口；主接口需校验 `request.pCodes ⊆ menu.pCodes`
- **API**：
  - Super 接口：开通菜单时自动使用菜单全部 `p_codes`（可加标识或独立路径区分）
  - 主接口：入参包含 roleId、menuId、pCodes（选中的权限 code 列表），出参为操作结果
- **前端**：角色管理页面在开通菜单时展示需同步的权限列表，默认全选，允许取消部分，将选中列表回传主接口
