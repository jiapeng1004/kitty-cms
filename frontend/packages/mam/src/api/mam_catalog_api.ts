import api from '../utils/api'
import type { MaterialPermissionCodeValue } from '@/constants/material_permission_code'
import { MATERIAL_SERVICE_PATH } from './constants'

const CATALOG_PREFIX = `${MATERIAL_SERVICE_PATH}/api/catalog`
const PERMISSION_PREFIX = `${MATERIAL_SERVICE_PATH}/api/catalogPermission`

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

export interface CatalogUpdateDTO {
  id: string
  name?: string
  parentId?: string
  sortNum?: number
}

/** 与后端 {@code CatalogPermissionNode} 对齐 */
export interface CatalogPermissionNode extends Omit<MaterialCatalogNode, 'children'> {
  permissionCode: string
  allowed: boolean
  children?: CatalogPermissionNode[]
}

/** 查询栏目树 */
export function queryCatalogTree(): Promise<MaterialCatalogNode[]> {
  return api.get(`${CATALOG_PREFIX}/tree`)
}

/** 查询指定角色的栏目树（节点含 {@link MaterialCatalogNode.permissionCodes}） */
export function catalogTreeWithRolePermission(roleId: string): Promise<MaterialCatalogNode[]> {
  return api.get(`${CATALOG_PREFIX}/permission/tree/role/${roleId}`)
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
  return api.post(`${CATALOG_PREFIX}`, dto)
}

/** 更新栏目（重命名/移动/排序） */
export function updateCatalog(dto: CatalogUpdateDTO): Promise<void> {
  return api.put(`${CATALOG_PREFIX}`, dto)
}

/** 删除栏目 */
export function deleteCatalog(catalogId: string): Promise<void> {
  return api.delete(`${CATALOG_PREFIX}/${catalogId}`)
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
  return api.get(`${PERMISSION_PREFIX}/check`, {
    params: { catalogId, permissionCode }
  })
}

/** 获取指定角色的栏目权限映射 */
export function getCatalogPermissionsByRoles(roleIds: string[]): Promise<Map<string, string[]>> {
  return api.post(`${PERMISSION_PREFIX}/map`, roleIds)
}

/** 获取当前用户的栏目权限映射 */
export function getCurrentUserCatalogPermissions(): Promise<Map<string, string[]>> {
  return api.get(`${PERMISSION_PREFIX}/tree/currentUser`)
}

/** 全量替换：对 roleIds 中每个角色删除可编辑权限后写入 permissionItems */
export function upsertCatalogPermission(dto: CatalogPermissionUpsertDTO): Promise<void> {
  return api.post(`${PERMISSION_PREFIX}/upsert`, dto)
}

/** 单格授权（即时勾选等场景） */
export function grantCatalogPermission(dto: CatalogPermissionCellDTO): Promise<void> {
  return api.post(`${PERMISSION_PREFIX}/grant`, dto)
}

/** 单格撤销 */
export function revokeCatalogPermission(dto: CatalogPermissionCellDTO): Promise<void> {
  return api.post(`${PERMISSION_PREFIX}/revoke`, dto)
}
