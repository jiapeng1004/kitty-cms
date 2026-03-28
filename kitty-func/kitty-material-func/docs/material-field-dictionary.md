## Material 字段语义字典（阶段1）

## 通用规则

- 公共标识统一使用 `public`，不使用 `null` 表示“公共”
- 树结构无父节点统一使用 `parentId=0`
- 个人栏目虚拟根使用 `pri_用户id` 命名空间

## catalog（栏目）

- `id`: 栏目主键或虚拟根标识
- `parent_id`: 父节点，顶层为 `0`，个人子栏目可为 `pri_用户id`
- `scope_type`: `public` / `private`
- `owner_user_id`: 私有栏目所属用户
- `sort_num`: 同级排序值

## resource（资源）

- `id`: 资源主键（唯一标识）
- `catalog_id`: 归属栏目（必填）
- `parent_id`: 父资源（文件夹）ID，无父级固定 `0`
- `type`: 资源类型（1视频/2音频/3图片/4文本/5Office/6其他/7文件夹）
- `title`: 资源标题

## 权限（预留）

- `role_id + catalog_id + permission_code + true`: 角色具备权限
- `public + catalog_id + permission_code + true`: 全体具备权限
- `public + catalog_id + permission_code + false`: 全体强制禁用，不可绕过
