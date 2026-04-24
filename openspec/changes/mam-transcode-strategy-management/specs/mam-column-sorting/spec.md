# MAM 栏目排序功能

## 1. 需求概述
为 MAM 媒资管理系统添加栏目排序功能，支持：
- 数据库存储排序字段
- 后端 API 支持排序查询和更新
- 前端展示排序并允许调整

## 2. 数据库设计
### 2.1 表结构修改
在 `kt_catalog` 表中已存在 `sort_num` 字段：
```sql
ALTER TABLE `kt_catalog` 
    ADD COLUMN `sort_num` int NOT NULL DEFAULT 0 COMMENT '排序值' AFTER `owner_user_id`;
```

## 3. 后端实现
### 3.1 实体类
```java
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("kt_catalog")
public class KtCatalog extends CommonEntity {
    @TableField("sort_num")
    private Integer sortNum;
}
```

### 3.2 服务层实现
```java
@Override
public List<CatalogNodeVO> findByParentId(String parentId) {
    return lambdaQuery()
            .eq(KtCatalog::getParentId, parentId)
            .orderByAsc(KtCatalog::getSortNum)
            .orderByAsc(KtCatalog::getName)
            .list()
            .stream()
            .map(CatalogBeansConvert.INSTANCE::mp2Node)
            .toList();
}
```

### 3.3 API 接口
```java
@Operation(description = "更新栏目（重命名/移动/排序）")
@PutMapping("/api/catalog")
@SaCheckPermission(MaterialPermissionCode.MATERIAL_CATALOG_TREE_VIEW)
@Override
public void updateCatalog(@RequestBody @Valid CatalogUpdateDTO dto) {
    catalogService.update(dto);
}
```

## 4. 前端实现
### 4.1 API 定义
```typescript
export interface CatalogUpdateDTO {
  id: string
  name?: string
  parentId?: string
  sortNum?: number
}
```

### 4.2 组件实现
前端已通过 `MaterialCatalogTreePanel.vue` 组件实现栏目树展示和排序更新功能。

## 5. 测试用例
1. 创建多个子栏目，验证排序顺序
2. 更新栏目排序值，验证顺序变化
3. 移动栏目到其他父目录，验证排序保持

## 6. 验收标准
- 栏目列表按 `sort_num` 升序展示
- 支持通过 API 更新 `sort_num` 字段
- 前端可展示和调整栏目排序
