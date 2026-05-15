import api from '../utils/api'

/**
 * Super 接口：按菜单全部 p_codes 为角色开通菜单及权限
 */
export function grantMenuWithPermissionsSuper(roleId: string, menuId: string): Promise<void> {
  return api.post('/api/role/menu/super/grant', null, {
    params: { roleId, menuId }
  })
}

/**
 * 主接口：按前端勾选的权限 code 为角色开通菜单及权限
 */
export function grantMenuWithPermissions(options: {
  roleId: string
  menuId: string
  pCodes: string[]
}): Promise<void> {
  return api.post('/api/role/menu/grant', options)
}
