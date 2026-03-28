## material-func 重建替换边界（阶段1）

## 可直接替换（低风险）

- `icu.jiapeng.kitty.material.api..`（新建，替代旧 `interfaces` 对外入口）
- `icu.jiapeng.kitty.material.<domain>..`（按业务域分包：`controller` / `service` / `entity` / `mapper` 等；不再保留 `material.domain..` 命名空间）
- `icu.jiapeng.kitty.material.support.web.GlobalExceptionHandler`（全局异常处理，横切能力放 `support`）

## 兼容保留（中风险，后续迁移）

- `icu.jiapeng.kitty.material.interfaces..`：标记为兼容层，短期保留避免接口突变
- `icu.jiapeng.kitty.material.resource..`：继续沿用既有仓库结构，逐步改造字段语义
- `icu.jiapeng.kitty.material.infrastructure.resource..`：先保留并逐步对齐新模型

## 待清理（高风险，需配合联调）

- 旧风格资源仓储和转换器中的历史字段语义（如 folderId）已改为 `parentId`，需要配套 SQL 与前端字段同步后再做彻底删除
- 与旧 controller 路由可能重叠的接口，在新 API 全量接管后统一下线

## 下一步迁移顺序建议

1. 先补齐栏目与资源查询新接口
2. 再迁移上传与存储链路
3. 最后清理旧接口包与冗余 DTO
