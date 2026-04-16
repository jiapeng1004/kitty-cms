/**
 * 与后端 `icu.jiapeng.kitty.material.permission.constants.MaterialPermissionCode` 字符串一致。
 */
export const MaterialPermissionCode = {
  MATERIAL_CATALOG_TREE_VIEW: 'material:catalog:tree:view',
  MATERIAL_RESOURCE_LIST_VIEW: 'material:resource:list:view',
  MATERIAL_RESOURCE_CREATE: 'material:resource:create',
  MATERIAL_RESOURCE_UPDATE: 'material:resource:update',
  MATERIAL_METADATA_TEMPLATE_MANAGE: 'material:metadata:template:manage',
  MATERIAL_METADATA_FIELD_MANAGE: 'material:metadata:field:manage',
  MATERIAL_TRANSCODE_POLICY_MANAGE: 'material:transcode:policy:manage',
  MATERIAL_MESSAGE_SEND: 'material:message:send',
  MATERIAL_MESSAGE_READ: 'material:message:read',
  MATERIAL_REVIEW_SUBMIT: 'material:review:submit',
  MATERIAL_REVIEW_APPROVE: 'material:review:approve',
  MATERIAL_CATALOG_PERMISSION_EDIT: 'material:catalog:permission:edit',
  MATERIAL_CATALOG_PERMISSION_VIEW: 'material:catalog:permission:view',
  MATERIAL_CATALOG_CREATE: 'material:catalog:create',
  MATERIAL_CATALOG_UPDATE: 'material:catalog:update',
  MATERIAL_CATALOG_DELETE: 'material:catalog:delete',
  MATERIAL_STORAGE_MANAGE: 'material:storage:manage'
} as const

export type MaterialPermissionCodeKey = keyof typeof MaterialPermissionCode
export type MaterialPermissionCodeValue =
  (typeof MaterialPermissionCode)[MaterialPermissionCodeKey]

/**
 * 与后端 `CatalogPermission` 枚举顺序、含义一致；用于 `kt_catalog_permission` / `requireOnCatalog` 维度。
 */
export const CATALOG_DIMENSION_PERMISSION_CODES: readonly MaterialPermissionCodeValue[] = [
  MaterialPermissionCode.MATERIAL_CATALOG_TREE_VIEW,
  MaterialPermissionCode.MATERIAL_CATALOG_UPDATE,
  MaterialPermissionCode.MATERIAL_CATALOG_DELETE,
  MaterialPermissionCode.MATERIAL_CATALOG_CREATE,
  MaterialPermissionCode.MATERIAL_METADATA_TEMPLATE_MANAGE,
  MaterialPermissionCode.MATERIAL_RESOURCE_LIST_VIEW,
  MaterialPermissionCode.MATERIAL_RESOURCE_CREATE,
  MaterialPermissionCode.MATERIAL_RESOURCE_UPDATE,
  MaterialPermissionCode.MATERIAL_TRANSCODE_POLICY_MANAGE,
  MaterialPermissionCode.MATERIAL_REVIEW_SUBMIT,
  MaterialPermissionCode.MATERIAL_REVIEW_APPROVE
]

/**
 * 迁移前 kt_catalog_permission 使用的短码，与 canonical 等价（与后端兼容逻辑一致）。
 */
export const LEGACY_CATALOG_PERMISSION_BY_CANONICAL: Readonly<
  Partial<Record<MaterialPermissionCodeValue, readonly string[]>>
> = {
  [MaterialPermissionCode.MATERIAL_CATALOG_TREE_VIEW]: ['view'],
  [MaterialPermissionCode.MATERIAL_CATALOG_UPDATE]: ['edit'],
  [MaterialPermissionCode.MATERIAL_CATALOG_CREATE]: ['add'],
  [MaterialPermissionCode.MATERIAL_CATALOG_DELETE]: ['delete']
}

/** 判断节点 permissionCodes 是否具备某栏目维度权限（含历史短码） */
export function catalogNodeHasPermission(
  permissionCodes: string[] | undefined,
  canonical: MaterialPermissionCodeValue
): boolean {
  if (!permissionCodes?.length) {
    return false
  }
  if (permissionCodes.includes(canonical)) {
    return true
  }
  const legacy = LEGACY_CATALOG_PERMISSION_BY_CANONICAL[canonical]
  if (!legacy?.length) {
    return false
  }
  return legacy.some((c) => permissionCodes.includes(c))
}
