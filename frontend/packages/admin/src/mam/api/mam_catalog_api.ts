import api from '@/utils/api'
import type {MaterialPermissionCodeValue} from '@/mam/constants/material_permission_code'

/** 与后端 {@code CatalogNodeVO} 对齐 */
export interface MaterialCatalogNode {
    id: string
    parentId: string
    name: string
    virtualRoot?: boolean
    sortNum?: number
    children?: MaterialCatalogNode[]
    permissionCodes?: string[]
}

/** 与后端 {@code CatalogCreateDTO} 对齐 */
export interface CatalogCreateDTO {
    name: string
    parentId: string
}

/** 与后端 `CatalogMovePosition` 一致，与 targetId 成对；INSIDE=新父=目标（弹窗选父、拖入目录内） */
export type CatalogMovePosition = 'BEFORE' | 'AFTER' | 'INSIDE'

export interface CatalogUpdateDTO {
    id: string
    name?: string
    sortNum?: number
    /** 与 position 成对；INSIDE 时即新父栏目 id，不再传单独的 parentId */
    targetId?: string
    position?: CatalogMovePosition
}

/** 与后端 {@code CatalogPermissionNode} 对齐 */
export interface CatalogPermissionNode extends Omit<MaterialCatalogNode, 'children'> {
    permissionCode: string
    allowed: boolean
    children?: CatalogPermissionNode[]
}

/** 查询栏目树 */
export function queryCatalogTree(): Promise<MaterialCatalogNode[]> {
    return api.get(`/api/material/catalog/tree`)
}

/** 查询指定角色的栏目树（节点含 {@link MaterialCatalogNode.permissionCodes}） */
export function catalogTreeWithRolePermission(roleId: string): Promise<MaterialCatalogNode[]> {
    return api.get(`/api/material/catalog/permission/tree/role/${roleId}`)
}

/**
 * 将栏目树 VO 转为授权页展示用节点（根据 permissionCode 计算 allowed）。
 */
export function adaptCatalogTreeForPermissionCode(
    nodes: MaterialCatalogNode[] | undefined,
    permissionCode: string
): CatalogPermissionNode[] {
    if (!nodes?.length) {
        return []
    }
    return nodes.map((n) => ({
        id: n.id,
        parentId: n.parentId,
        name: n.name,
        virtualRoot: n.virtualRoot,
        sortNum: n.sortNum,
        permissionCode,
        allowed: !!(n.permissionCodes?.includes(permissionCode)),
        children: adaptCatalogTreeForPermissionCode(n.children, permissionCode)
    }))
}

/** 新建栏目 */
export function createCatalog(dto: CatalogCreateDTO): Promise<string> {
    return api.post(`/api/material/catalog`, dto)
}

/** 更新栏目（重命名/移动/排序） */
export function updateCatalog(dto: CatalogUpdateDTO): Promise<void> {
    return api.put(`/api/material/catalog`, dto)
}

/** 删除栏目 */
export function deleteCatalog(catalogId: string): Promise<void> {
    return api.delete(`/api/material/catalog/${catalogId}`)
}

// ========== 权限相关 ==========

/** 与后端 {@code CatalogPermissionUpsertDTO} 对齐：先删 roleIds 下可编辑规则，再写入矩阵 */
export interface CatalogPermissionUpsertDTO {
    roleIds: string[]
    /** 仅替换这些权限码的可编辑行；不传则删除这些角色下全部可编辑规则（慎用） */
    permissionCodesReplace?: string[]
    permissionItems: CatalogPermissionUpsertItem[]
}

export interface CatalogPermissionUpsertItem {
    roleId: string
    catalogId: string
    permissionCodes: string[]
}

/** 与后端 {@code CatalogPermissionCellDTO} 对齐 */
export interface CatalogPermissionCellDTO {
    roleId: string
    catalogId: string
    permissionCode: string
}

/** 检查当前用户是否在指定栏目上有指定权限（permissionCode 为 MaterialPermissionCode 字面量） */
export function checkCatalogPermission(
    catalogId: string,
    permissionCode: MaterialPermissionCodeValue | string
): Promise<boolean> {
    return api.get(`/api/material/catalogPermission/check`, {
        params: {catalogId, permissionCode}
    })
}

/** 获取指定角色的栏目权限映射 */
export function getCatalogPermissionsByRoles(roleIds: string[]): Promise<Map<string, string[]>> {
    return api.post(`/api/material/catalogPermission/map`, roleIds)
}

/** 获取当前用户的栏目权限映射 */
export function getCurrentUserCatalogPermissions(): Promise<Map<string, string[]>> {
    return api.get(`/api/material/catalogPermission/tree/currentUser`)
}

/** 全量替换：对 roleIds 中每个角色删除可编辑权限后写入 permissionItems */
export function upsertCatalogPermission(dto: CatalogPermissionUpsertDTO): Promise<void> {
    return api.post(`/api/material/catalogPermission/upsert`, dto)
}

/** 单格授权（即时勾选等场景） */
export function grantCatalogPermission(dto: CatalogPermissionCellDTO): Promise<void> {
    return api.post(`/api/material/catalogPermission/grant`, dto)
}

/** 单格撤销 */
export function revokeCatalogPermission(dto: CatalogPermissionCellDTO): Promise<void> {
    return api.post(`/api/material/catalogPermission/revoke`, dto)
}
